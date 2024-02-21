package uk.gov.companieshouse.filinghistory.api.service;

import java.util.Optional;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

public interface Service {

    Optional<FilingHistoryDocument> findExistingFilingHistory(final String transactionId);

    void insertFilingHistory(final FilingHistoryDocument documentToSave);
    void updateFilingHistory(final FilingHistoryDocument documentToSave, final FilingHistoryDocument existingDocument);
}
