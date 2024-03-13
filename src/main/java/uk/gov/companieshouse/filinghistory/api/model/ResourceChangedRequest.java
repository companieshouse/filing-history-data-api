package uk.gov.companieshouse.filinghistory.api.model;

public record ResourceChangedRequest(FilingHistoryDocument filingHistoryDocument, boolean isDelete) {

}
