package uk.gov.companieshouse.filinghistory.api.model;

import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

import java.util.List;

public record ResourceChangedRequest(FilingHistoryDocument filingHistoryDocument, boolean isDelete, List<String> fieldsChanged) {

}
