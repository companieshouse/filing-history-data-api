package uk.gov.companieshouse.filinghistory.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

public interface UpsertProcessor {

    void processFilingHistory(final String transactionId, final InternalFilingHistoryApi request) throws JsonProcessingException;
}
