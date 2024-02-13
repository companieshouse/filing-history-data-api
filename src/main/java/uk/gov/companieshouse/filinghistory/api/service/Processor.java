package uk.gov.companieshouse.filinghistory.api.service;

import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

public interface Processor {

    ServiceResult processFilingHistory(final String transactionId, final InternalFilingHistoryApi request);

    ExternalData processGetSingleFilingHistory(String companyNumber, String transactionId);
}
