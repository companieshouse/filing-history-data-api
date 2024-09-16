package uk.gov.companieshouse.filinghistory.api.service;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
        apiClient.callResourceChanged(new ResourceChangedRequest(docToInsert, false, null));
    }

    @Override
    public void updateFilingHistory(FilingHistoryDocument docToUpdate) {
        repository.update(docToUpdate);
        apiClient.callResourceChanged(new ResourceChangedRequest(docToUpdate, false, null));
    }

    @Override
    public void updateDocumentMetadata(FilingHistoryDocument docToUpdate) {
        repository.update(docToUpdate);
        List<String> fieldsChanged = new ArrayList<>();
        fieldsChanged.add("links.document_metadata");
        apiClient.callResourceChanged(new ResourceChangedRequest(docToUpdate, false, fieldsChanged));
    }

    @Transactional
    @Override
    public void deleteExistingFilingHistory(FilingHistoryDocument existingDocument) {
        repository.deleteById(existingDocument.getTransactionId());
        apiClient.callResourceChanged(new ResourceChangedRequest(existingDocument, true, null));
    }

    private void throwBadGatewayException(final int statusCode) {
        LOGGER.info("Resource changed endpoint responded with: %s".formatted(statusCode), DataMapHolder.getLogMap());
        throw new BadGatewayException("Error calling resource changed endpoint");
    }
}
