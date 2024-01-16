package uk.gov.companieshouse.filinghistory.api.service;

import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.ServiceResult;

public interface Service {

    ServiceResult upsertFilingHistory(final String transactionId, final InternalFilingHistoryApi request);

}
