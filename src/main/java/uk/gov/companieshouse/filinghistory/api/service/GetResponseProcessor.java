package uk.gov.companieshouse.filinghistory.api.service;

import uk.gov.companieshouse.api.filinghistory.ExternalData;

public interface GetResponseProcessor {
    ExternalData processGetSingleFilingHistory(final String companyNumber, final String transactionId);
}
