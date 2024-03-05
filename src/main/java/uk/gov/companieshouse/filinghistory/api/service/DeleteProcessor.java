package uk.gov.companieshouse.filinghistory.api.service;

public interface DeleteProcessor {


    void processFilingHistoryDelete(String transactionId);
}
