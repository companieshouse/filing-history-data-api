package uk.gov.companieshouse.filinghistory.api.service;

import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

public interface UpsertProcessor {

    ServiceResult processFilingHistory(final String transactionId, final InternalFilingHistoryApi request);
}
