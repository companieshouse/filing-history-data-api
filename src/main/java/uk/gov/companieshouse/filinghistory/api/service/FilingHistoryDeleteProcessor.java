package uk.gov.companieshouse.filinghistory.api.service;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.isDeltaStale;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.exception.BadRequestException;
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
        final String deltaAt = request.deltaAt();
        if (StringUtils.isBlank(deltaAt)) {
            LOGGER.error("deltaAt missing from delete request", DataMapHolder.getLogMap());
            throw new BadRequestException("deltaAt is null or empty");
        }
        final String entityId = request.entityId();
        final String companyNumber = request.companyNumber();
        final String transactionId = request.transactionId();

        filingHistoryService.findFilingHistoryByEntityId(entityId)
                .ifPresentOrElse(
                        deleteAggregate -> deleteMapperDelegator.delegateDelete(entityId, deleteAggregate, deltaAt)
                                .ifPresentOrElse(
                                        updatedDocument -> {
                                            LOGGER.info("Removing child", DataMapHolder.getLogMap());
                                            filingHistoryService.updateFilingHistory(updatedDocument, companyNumber,
                                                    transactionId);
                                        },
                                        () -> {
                                            FilingHistoryDocument parentDocument = deleteAggregate.getDocument();
                                            deltaAtCheck(deltaAt, parentDocument.getDeltaAt());
                                            LOGGER.info("Deleting parent", DataMapHolder.getLogMap());
                                            filingHistoryService.deleteExistingFilingHistory(parentDocument,
                                                    companyNumber, transactionId);
                                        }),
                        () -> filingHistoryService.findExistingFilingHistory(transactionId, companyNumber)
                                .ifPresentOrElse(
                                        parentDocument -> {
                                            LOGGER.info("Delete for non-existent child", DataMapHolder.getLogMap());
                                            filingHistoryService.callResourceChangedAbsentChild(companyNumber,
                                                    transactionId);
                                        },
                                        () -> {
                                            LOGGER.info("Delete for non-existent document", DataMapHolder.getLogMap());
                                            filingHistoryService.callResourceChangedAbsentParent(companyNumber,
                                                    transactionId);
                                        })
                );
    }

    private void deltaAtCheck(String requestDeltaAt, String existingDeltaAt) {
        if (isDeltaStale(requestDeltaAt, existingDeltaAt)) {
            LOGGER.error("Stale delta received; request delta_at: [%s] is not after existing delta_at: [%s]".formatted(
                    requestDeltaAt, existingDeltaAt), DataMapHolder.getLogMap());
            throw new ConflictException("Stale delete delta");
        }
    }
}
