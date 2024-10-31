package uk.gov.companieshouse.filinghistory.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.client.ResourceChangedApiClient;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeleteAggregate;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryListAggregate;
import uk.gov.companieshouse.filinghistory.api.repository.Repository;

@Component
public class FilingHistoryService implements Service {

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
    public void insertFilingHistory(final FilingHistoryDocument docToInsert, String companyNumber,
            String transactionId) {
        repository.insert(docToInsert);
        apiClient.callResourceChanged(new ResourceChangedRequest(docToInsert, companyNumber, transactionId,
                false, null));
    }

    @Override
    public void updateFilingHistory(FilingHistoryDocument docToUpdate, String companyNumber, String transactionId) {
        repository.update(docToUpdate);
        apiClient.callResourceChanged(new ResourceChangedRequest(docToUpdate, companyNumber, transactionId,
                false, null));
    }

    @Override
    public void updateDocumentMetadata(FilingHistoryDocument docToUpdate, String companyNumber, String transactionId) {
        repository.update(docToUpdate);
        List<String> fieldsChanged = new ArrayList<>();
        fieldsChanged.add("links.document_metadata");
        apiClient.callResourceChanged(new ResourceChangedRequest(docToUpdate, companyNumber, transactionId,
                false, fieldsChanged));
    }

    // Remove and check tests
    @Override
    public void deleteExistingFilingHistory(FilingHistoryDocument existingDocument, String companyNumber,
            String transactionId ) {
        repository.deleteById(existingDocument.getTransactionId());
        apiClient.callResourceChanged(new ResourceChangedRequest(existingDocument, companyNumber, transactionId,
                true, null));
    }

    @Override
    public void callResourceChangedForAbsentDeletedData(String companyNumber, String transactionId) {
        apiClient.callResourceChanged(new ResourceChangedRequest(null, companyNumber, transactionId,
                true, null));
    }
}
