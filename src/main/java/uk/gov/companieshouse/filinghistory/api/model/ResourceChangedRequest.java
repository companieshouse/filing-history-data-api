package uk.gov.companieshouse.filinghistory.api.model;

public record ResourceChangedRequest(String contextId, FilingHistoryDocument filingHistoryDocument,
                                     boolean isDelete) {

}
