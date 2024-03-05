package uk.gov.companieshouse.filinghistory.api.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.exception.NotFoundException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class FilingHistoryDeleteProcessor implements DeleteProcessor{

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);
    private final Service filingHistoryService;

    public FilingHistoryDeleteProcessor(Service filingHistoryService) {
        this.filingHistoryService = filingHistoryService;
    }

    @Override
    public void processFilingHistoryDelete(String transactionId) {

        filingHistoryService.findExistingFilingHistoryById(transactionId)
                .ifPresentOrElse(
                        filingHistoryService::deleteExistingFilingHistory,
                        () -> {
                            LOGGER.error("Record to delete could not be found in MongoDB",
                                    DataMapHolder.getLogMap());
                            throw new NotFoundException("Record to delete could not be found in MongoDB");
                        }
                );
    }
}
