package uk.gov.companieshouse.filinghistory.api.service;

import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDeleteRequest;

public interface DeleteProcessor {

    void processFilingHistoryDelete(FilingHistoryDeleteRequest request);
}
