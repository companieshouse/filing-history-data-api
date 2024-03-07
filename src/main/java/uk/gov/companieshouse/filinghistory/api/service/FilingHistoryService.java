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
import uk.gov.companieshouse.filinghistory.api.repository.Repository;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class FilingHistoryService extends AbstractService implements Service {

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
    public void insertFilingHistory(final FilingHistoryDocument documentToSave, Boolean isDelete) {
        saveOrDeleteDocument(documentToSave, false);

        ApiResponse<Void> result = callResourceChangedApi(documentToSave, false);

        handleResponseAndApplyCompensatoryTransaction(result, documentToSave, null);
    }

    @Override
    public void updateFilingHistory(FilingHistoryDocument documentToSave, FilingHistoryDocument originalDocumentCopy, Boolean isDelete) {
        saveOrDeleteDocument(documentToSave, false);

        ApiResponse<Void> result = callResourceChangedApi(documentToSave, false);

        handleResponseAndApplyCompensatoryTransaction(result, documentToSave, originalDocumentCopy);
    }

    @Override
    @Transactional
    public void deleteExistingFilingHistory(FilingHistoryDocument existingDocument) {
        saveOrDeleteDocument(existingDocument, true);

        callResourceChangedApi(existingDocument, true);
        }

    @Override
    public Optional<FilingHistoryDocument> findExistingFilingHistoryById(String transactionId) {
        return repository.findById(transactionId);
    }

    @Override
    public void saveOrDeleteDocument(FilingHistoryDocument document, Boolean isDelete) {
        if(Boolean.FALSE.equals(isDelete)) {
            repository.save(document);
        } else {
            repository.deleteById(document.getTransactionId());
        }
    }

    @Override
    public ApiResponse<Void> callResourceChangedApi(FilingHistoryDocument documentToSaveDelete, Boolean isDelete) {
        return apiClient.callResourceChanged(
                new ResourceChangedRequest(DataMapHolder.getRequestId(), documentToSaveDelete.getCompanyNumber(),
                        documentToSaveDelete.getTransactionId(), null, isDelete));
    }

    @Override
    public void handleResponseAndApplyCompensatoryTransaction(ApiResponse<Void> result,
            FilingHistoryDocument documentToSave, FilingHistoryDocument originalDocumentCopy) {

        if (!HttpStatus.valueOf(result.getStatusCode()).is2xxSuccessful()) {
            if (originalDocumentCopy == null) {
                repository.deleteById(documentToSave.getTransactionId());
                LOGGER.info("Deleting previously inserted document", DataMapHolder.getLogMap());
            } else {
                repository.save(originalDocumentCopy);
                LOGGER.info("Reverting previously inserted document", DataMapHolder.getLogMap());
            }
            LOGGER.error("Resource changed endpoint unavailable", DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("Resource changed endpoint unavailable");
        }
    }
}
