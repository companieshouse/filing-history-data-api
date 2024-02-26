package uk.gov.companieshouse.filinghistory.api.service;

import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

public interface UpsertProcessor {

    void processFilingHistory(final String transactionId,
                              final String companyNumber,
                              final InternalFilingHistoryApi request);
}
