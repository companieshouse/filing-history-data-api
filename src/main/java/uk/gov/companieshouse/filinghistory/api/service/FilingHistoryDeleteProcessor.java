package uk.gov.companieshouse.filinghistory.api.service;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.isDeltaStale;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.mapper.delete.DeleteMapperDelegator;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDeleteRequest;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class FilingHistoryDeleteProcessor implements DeleteProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);
    private final Service filingHistoryService;
    private final DeleteMapperDelegator deleteMapperDelegator;

    public FilingHistoryDeleteProcessor(Service filingHistoryService, DeleteMapperDelegator deleteMapperDelegator) {
        this.filingHistoryService = filingHistoryService;
        this.deleteMapperDelegator = deleteMapperDelegator;
    }

    @Override
    public void processFilingHistoryDelete(FilingHistoryDeleteRequest request) {
        filingHistoryService.findFilingHistoryByEntityId(request.entityId())
                .ifPresentOrElse(
                        deleteAggregate -> deleteMapperDelegator.delegateDelete(request.entityId(),
                                        deleteAggregate, request.deltaAt())
                                .ifPresentOrElse(
                                        updatedDocument -> {
                                            LOGGER.info("Removing child", DataMapHolder.getLogMap());
                                            filingHistoryService.updateFilingHistory(updatedDocument,
                                                    request.companyNumber(), request.transactionId());
                                        },
                                        () -> {
                                            FilingHistoryDocument parentDocument = deleteAggregate.getDocument();
                                            deltaAtCheck(request.deltaAt(), parentDocument);
                                            LOGGER.info("Deleting parent", DataMapHolder.getLogMap());
                                            filingHistoryService.deleteExistingFilingHistory(parentDocument,
                                                    request.companyNumber(), request.transactionId());
                                        }),
                        () -> {
                            LOGGER.info("Delete for non-existent document", DataMapHolder.getLogMap());
                            filingHistoryService.callResourceChangedForAbsentDeletedData(request.companyNumber(),
                                    request.transactionId());
                        }
                );
    }

    private void deltaAtCheck(String requestDeltaAt, FilingHistoryDocument document) {
        if (isDeltaStale(requestDeltaAt, document.getDeltaAt())) {
            LOGGER.error("Stale delta received; request delta_at: [%s] is not after existing delta_at: [%s]".formatted(
                    requestDeltaAt, document.getDeltaAt()), DataMapHolder.getLogMap());
            throw new ConflictException("Stale delta for delete");
        }
    }
}
