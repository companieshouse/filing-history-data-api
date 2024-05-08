package uk.gov.companieshouse.filinghistory.api.service;

import java.util.List;
import java.util.Optional;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryListAggregate;

public interface Service {

    Optional<FilingHistoryListAggregate> findCompanyFilingHistoryList(String companyNumber, int startIndex,
            int itemsPerPage, List<String> categories);

    Optional<FilingHistoryDocument> findExistingFilingHistory(final String transactionId, final String companyNumber);

    Optional<FilingHistoryDocument> findFilingHistoryByEntityId(String entityId);

    void insertFilingHistory(final FilingHistoryDocument documentToSave);

    void updateFilingHistory(final FilingHistoryDocument documentToSave, FilingHistoryDocument originalDocumentCopy);

    void deleteExistingFilingHistory(FilingHistoryDocument existingDocument);
}
