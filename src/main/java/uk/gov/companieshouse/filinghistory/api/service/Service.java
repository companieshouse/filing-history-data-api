package uk.gov.companieshouse.filinghistory.api.service;

import java.util.Optional;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

public interface Service {

    Optional<FilingHistoryDocument> findExistingFilingHistory(final String transactionId);

    ServiceResult insertFilingHistory(final FilingHistoryDocument documentToSave);
    ServiceResult updateFilingHistory(final FilingHistoryDocument documentToSave, final FilingHistoryDocument existingDocument);
}
