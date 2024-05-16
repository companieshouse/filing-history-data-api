package uk.gov.companieshouse.filinghistory.api.service;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.filinghistory.api.client.ResourceChangedApiClient;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeleteAggregate;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryListAggregate;
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
    public Optional<FilingHistoryListAggregate> findCompanyFilingHistoryList(String companyNumber,
            int startIndex,
            int itemsPerPage, List<String> categories) {
        List<String> categoryList = categories == null ? new ArrayList<>() : new ArrayList<>(categories);
        if (categoryList.contains("confirmation-statement")) {
            categoryList.add("annual-return");
        }
        if (categoryList.contains("incorporation")) {
            categoryList.addAll(
                    List.of("change-of-constitution", "change-of-name", "court-order",
                            "gazette", "reregistration", "resolution", "restoration"));
        }

        return Optional.of(repository.findCompanyFilingHistory(companyNumber, startIndex, itemsPerPage, categoryList))
                .filter(listAggregate -> listAggregate.getTotalCount() > 0);
    }

    @Override
    public Optional<FilingHistoryDocument> findExistingFilingHistory(final String transactionId,
            final String companyNumber) {
        return repository.findByIdAndCompanyNumber(transactionId, companyNumber);
    }

    @Override
    public Optional<FilingHistoryDeleteAggregate> findFilingHistoryByEntityId(String entityId) {
        return repository.findByEntityId(entityId);
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
        ApiResponse<Void> response = apiClient.callResourceChanged(new ResourceChangedRequest(existingDocument, true));
        if (!HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful()) {
            throwServiceUnavailable();
        }
    }

    private void handleTransaction(FilingHistoryDocument documentToSave, FilingHistoryDocument originalDocumentCopy) {
        repository.save(documentToSave);
        ApiResponse<Void> result = apiClient.callResourceChanged(new ResourceChangedRequest(documentToSave, false));
        if (!HttpStatus.valueOf(result.getStatusCode()).is2xxSuccessful()) {
            if (originalDocumentCopy == null) {
                repository.deleteById(documentToSave.getTransactionId());
                LOGGER.info("Deleting previously inserted document", DataMapHolder.getLogMap());
            } else {
                repository.save(originalDocumentCopy);
                LOGGER.info("Reverting previously updated document", DataMapHolder.getLogMap());
            }
            throwServiceUnavailable();
        }
    }

    private void throwServiceUnavailable() {
        LOGGER.error("Resource changed endpoint unavailable", DataMapHolder.getLogMap());
        throw new ServiceUnavailableException("Resource changed endpoint unavailable");
    }
}
