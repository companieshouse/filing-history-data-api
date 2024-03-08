package uk.gov.companieshouse.filinghistory.api.service;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.filinghistory.api.client.ResourceChangedApiClient;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequestBuilder;
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

    @Override
    public Optional<FilingHistoryDocument> findExistingFilingHistory(final String transactionId,
                                                                     final String companyNumber) {
        return repository.findByIdAndCompanyNumber(transactionId, companyNumber);
    }

    @Override
    public void insertFilingHistory(final FilingHistoryDocument documentToSave) {
        handleTransaction(documentToSave, null);
    }

    @Override
    public void updateFilingHistory(FilingHistoryDocument documentToSave, FilingHistoryDocument originalDocumentCopy) {
        handleTransaction(documentToSave, originalDocumentCopy);
    }

    @Transactional
    @Override
    public void deleteExistingFilingHistory(FilingHistoryDocument existingDocument) {
        repository.deleteById(existingDocument.getTransactionId());
        ApiResponse<Void> response = apiClient.callResourceChanged(
                buildBaseResourceChangedRequest(existingDocument)
                .filingHistoryData(existingDocument)
                .isDelete(true)
                .build());
        if (!HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful()) {
            throwServiceUnavailable();
        }
    }

    @Override
    public Optional<FilingHistoryDocument> findExistingFilingHistoryById(String transactionId) {
        return repository.findById(transactionId);
    }

    private void handleTransaction(FilingHistoryDocument documentToSave, FilingHistoryDocument originalDocumentCopy) {
        repository.save(documentToSave);

        ApiResponse<Void> result = apiClient.callResourceChanged(
                buildBaseResourceChangedRequest(documentToSave).build());

        if (!HttpStatus.valueOf(result.getStatusCode()).is2xxSuccessful()) {
            if (originalDocumentCopy == null) {
                repository.deleteById(documentToSave.getTransactionId());
                LOGGER.info("Deleting previously inserted document", DataMapHolder.getLogMap());
            } else {
                repository.save(originalDocumentCopy);
                LOGGER.info("Reverting previously inserted document", DataMapHolder.getLogMap());
            }
            throwServiceUnavailable();
        }
    }

    private ResourceChangedRequestBuilder buildBaseResourceChangedRequest(FilingHistoryDocument document) {
        return ResourceChangedRequestBuilder.builder()
                .contextId(DataMapHolder.getRequestId())
                .companyNumber(document.getCompanyNumber())
                .transactionId(document.getTransactionId());
    }

    private void throwServiceUnavailable() {
        LOGGER.error("Resource changed endpoint unavailable", DataMapHolder.getLogMap());
        throw new ServiceUnavailableException("Resource changed endpoint unavailable");
    }
}
