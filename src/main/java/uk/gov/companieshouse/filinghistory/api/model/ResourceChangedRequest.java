package uk.gov.companieshouse.filinghistory.api.model;

import java.util.List;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

public record ResourceChangedRequest(FilingHistoryDocument filingHistoryDocument, boolean isDelete, List<String> fieldsChanged) {

}
