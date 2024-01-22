package uk.gov.companieshouse.filinghistory.api.service;

import java.util.Optional;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

public interface Service {

    Optional<FilingHistoryDocument> findExistingFilingHistory(final String transactionId);

    ServiceResult saveFilingHistory(final FilingHistoryDocument document);

}
