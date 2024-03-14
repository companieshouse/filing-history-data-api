package uk.gov.companieshouse.filinghistory.api.service;

import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryList;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryListParams;

public interface GetResponseProcessor {
    ExternalData processGetSingleFilingHistory(final String companyNumber, final String transactionId);
    FilingHistoryList processGetCompanyFilingHistoryList(final FilingHistoryListParams params);
}
