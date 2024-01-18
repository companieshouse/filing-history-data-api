package uk.gov.companieshouse.filinghistory.api.service;

import java.util.Optional;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.ServiceResult;
import uk.gov.companieshouse.filinghistory.api.repository.Repository;

public class FilingHistoryService implements Service {

    private final Repository repository;

    public FilingHistoryService(Repository repository) {
        this.repository = repository;
    }

    public Optional<FilingHistoryDocument> findExistingFilingHistory(final String transactionId) {
        return repository.findById(transactionId);
    }

    public ServiceResult saveFilingHistory(final FilingHistoryDocument document) {
        repository.save(document);
        return ServiceResult.UPSERT_SUCCESSFUL;
    }
}
