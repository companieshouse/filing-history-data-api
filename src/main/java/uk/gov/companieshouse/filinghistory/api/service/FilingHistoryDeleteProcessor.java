package uk.gov.companieshouse.filinghistory.api.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.exception.NotFoundException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.mapper.delete.DeleteMapperDelegator;
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
    public void processFilingHistoryDelete(String entityId) {
        filingHistoryService.findFilingHistoryByEntityId(entityId)
                .ifPresentOrElse(
                        document -> deleteMapperDelegator.delegateDelete(entityId, document)
                                .ifPresentOrElse(
                                        updatedDocument -> {
                                            LOGGER.info("Removing child with _entity_id: [%s]".formatted(entityId),
                                                    DataMapHolder.getLogMap());
                                            filingHistoryService.updateFilingHistory(updatedDocument, document);
                                        },
                                        () -> {
                                            LOGGER.info("Deleting parent with _entity_id: [%s]".formatted(entityId),
                                                    DataMapHolder.getLogMap());
                                            filingHistoryService.deleteExistingFilingHistory(document);
                                        }),
                        () -> {
                            LOGGER.error("Document to delete not found", DataMapHolder.getLogMap());
                            throw new NotFoundException("Document to delete not found");
                        }
                );
    }
}
