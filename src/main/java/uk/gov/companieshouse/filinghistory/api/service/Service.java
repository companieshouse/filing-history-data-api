package uk.gov.companieshouse.filinghistory.api.service;

import java.util.List;
import java.util.Optional;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeleteAggregate;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryListAggregate;

public interface Service {

    Optional<FilingHistoryListAggregate> findCompanyFilingHistoryList(String companyNumber, int startIndex,
            int itemsPerPage, List<String> categories);

    Optional<FilingHistoryDocument> findExistingFilingHistory(final String transactionId, final String companyNumber);

    Optional<FilingHistoryDeleteAggregate> findFilingHistoryByEntityId(String entityId);

    void insertFilingHistory(final FilingHistoryDocument docToInsert, String companyNumber, String transactionId);

    void updateFilingHistory(final FilingHistoryDocument docToUpdate, String companyNumber, String transactionId);

    void updateDocumentMetadata(final FilingHistoryDocument docToUpdate, String companyNumber, String transactionId);

    void deleteExistingFilingHistory(FilingHistoryDocument existingDocument, String companyNumber, String transactionId);

    void callResourceChangedForAbsentDeletedData(String companyNumber, String transactionId);
}
