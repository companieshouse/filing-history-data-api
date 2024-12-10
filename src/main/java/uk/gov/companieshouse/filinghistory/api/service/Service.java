package uk.gov.companieshouse.filinghistory.api.service;

import java.util.List;
import java.util.Optional;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryListAggregate;

public interface Service {

    Optional<FilingHistoryListAggregate> findCompanyFilingHistoryList(String companyNumber, int startIndex,
            int itemsPerPage, List<String> categories);

    Optional<FilingHistoryDocument> findExistingFilingHistory(final String transactionId, final String companyNumber);

    void insertFilingHistory(FilingHistoryDocument docToInsert, String companyNumber, String transactionId);

    void updateFilingHistory(FilingHistoryDocument docToUpdate, String companyNumber, String transactionId);

    void updateDocumentMetadata(FilingHistoryDocument docToUpdate, String companyNumber, String transactionId);

    void deleteExistingFilingHistory(FilingHistoryDocument existingDocument, String companyNumber,
            String transactionId);

    void callResourceChangedAbsentParent(String companyNumber, String transactionId);

    void callResourceChangedAbsentChild(String companyNumber, String transactionId);
}
