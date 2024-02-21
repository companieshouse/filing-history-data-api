package uk.gov.companieshouse.filinghistory.api.service;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.filinghistory.api.client.ResourceChangedApiClient;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;
import uk.gov.companieshouse.filinghistory.api.repository.Repository;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class FilingHistoryService implements Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final ResourceChangedApiClient apiClient;
    private final Repository repository;

    public FilingHistoryService(ResourceChangedApiClient apiClient, Repository repository) {
        this.apiClient = apiClient;
        this.repository = repository;
    }

    public Optional<FilingHistoryDocument> findExistingFilingHistory(final String transactionId) {
        return repository.findById(transactionId);
    }

    @Override
    public void insertFilingHistory(final FilingHistoryDocument documentToSave) {
        handleTransaction(documentToSave, null);
    }

    @Override
    public void updateFilingHistory(FilingHistoryDocument documentToSave,
                                             FilingHistoryDocument existingDocument) {
        handleTransaction(documentToSave, existingDocument);
    }

    private void handleTransaction(FilingHistoryDocument documentToSave, FilingHistoryDocument existingDocument) {
        repository.save(documentToSave);

        ApiResponse<Void> result = apiClient.callResourceChanged(
                new ResourceChangedRequest(DataMapHolder.getRequestId(), documentToSave.getCompanyNumber(),
                        documentToSave.getTransactionId(), null, false));

        if (!HttpStatus.valueOf(result.getStatusCode()).is2xxSuccessful()) {
            repository.rollBackToOriginalState(existingDocument, documentToSave.getTransactionId());
            LOGGER.error("Resource changed endpoint unavailable", DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("Resource changed endpoint unavailable");
        }
    }
}
