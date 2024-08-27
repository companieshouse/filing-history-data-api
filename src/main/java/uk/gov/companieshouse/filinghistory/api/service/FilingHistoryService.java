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
import uk.gov.companieshouse.filinghistory.api.exception.BadGatewayException;
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

        List<String> filingHistoryIds = repository.findCompanyFilingHistoryIds(companyNumber, startIndex,
                itemsPerPage, categoryList).getIds();

        List<FilingHistoryDocument> documents = repository.findFullFilingHistoryDocuments(filingHistoryIds);

        long totalCount = repository.countTotal(companyNumber, categoryList);

        return Optional.of(new FilingHistoryListAggregate().documentList(documents).totalCount(totalCount))
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
    public void insertFilingHistory(final FilingHistoryDocument docToInsert) {
        repository.insert(docToInsert);
        ApiResponse<Void> result = apiClient.callResourceChanged(
                new ResourceChangedRequest(docToInsert, false, null));
        if (!HttpStatus.valueOf(result.getStatusCode()).is2xxSuccessful()) {
            repository.deleteById(docToInsert.getTransactionId());
            LOGGER.info("Deleting previously inserted document", DataMapHolder.getLogMap());
            throwBadGatewayException(result.getStatusCode());
        }
    }

    @Override
    public void updateFilingHistory(FilingHistoryDocument docToUpdate, FilingHistoryDocument originalDocumentCopy) {
        repository.update(docToUpdate);
        ApiResponse<Void> result = apiClient.callResourceChanged(
                new ResourceChangedRequest(docToUpdate, false, null));
        if (!HttpStatus.valueOf(result.getStatusCode()).is2xxSuccessful()) {
            Long originalVersion = originalDocumentCopy.getVersion();
            originalDocumentCopy.version(originalVersion == null ? 0 : originalVersion + 1);
            repository.update(originalDocumentCopy);
            LOGGER.info("Reverting previously updated document", DataMapHolder.getLogMap());
            throwBadGatewayException(result.getStatusCode());
        }
    }

    @Override
    public void updateDocumentMetadata(FilingHistoryDocument docToUpdate) {
        repository.update(docToUpdate);
        List<String> fieldsChanged = new ArrayList<>();
        fieldsChanged.add("links.document_metadata");
        ApiResponse<Void> result = apiClient.callResourceChanged(
                new ResourceChangedRequest(docToUpdate, false, fieldsChanged));
        if (!HttpStatus.valueOf(result.getStatusCode()).is2xxSuccessful()) {
            throwBadGatewayException(result.getStatusCode());
        }
    }

    @Transactional
    @Override
    public void deleteExistingFilingHistory(FilingHistoryDocument existingDocument) {
        repository.deleteById(existingDocument.getTransactionId());
        ApiResponse<Void> response = apiClient.callResourceChanged(
                new ResourceChangedRequest(existingDocument, true, null));
        if (!HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful()) {
            throwBadGatewayException(response.getStatusCode());
        }
    }

    private void throwBadGatewayException(final int statusCode) {
        LOGGER.error("Resource changed endpoint responded with: %s".formatted(statusCode), DataMapHolder.getLogMap());
        throw new BadGatewayException("Error calling resource changed endpoint");
    }
}
