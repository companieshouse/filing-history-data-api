package uk.gov.companieshouse.filinghistory.api.service;

import java.util.Optional;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

public interface Service {

    Optional<FilingHistoryDocument> findExistingFilingHistory(final String transactionId, final String companyNumber);

    void insertFilingHistory(final FilingHistoryDocument documentToSave, Boolean isDelete);
    void updateFilingHistory(final FilingHistoryDocument documentToSave, FilingHistoryDocument originalDocumentCopy, Boolean isDelete);

    void deleteExistingFilingHistory(FilingHistoryDocument existingDocument);

    Optional<FilingHistoryDocument> findExistingFilingHistoryById(String transactionId);
}
