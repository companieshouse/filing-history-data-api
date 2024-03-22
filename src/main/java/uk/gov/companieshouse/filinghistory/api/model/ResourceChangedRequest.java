package uk.gov.companieshouse.filinghistory.api.model;

import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

public record ResourceChangedRequest(FilingHistoryDocument filingHistoryDocument, boolean isDelete) {

}
