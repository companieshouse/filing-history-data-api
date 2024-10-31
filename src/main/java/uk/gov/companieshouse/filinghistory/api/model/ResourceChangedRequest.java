package uk.gov.companieshouse.filinghistory.api.model;

import java.util.List;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

public record ResourceChangedRequest(FilingHistoryDocument filingHistoryDocument, String companyNumber,
                                     String transactionId, boolean isDelete, List<String> fieldsChanged) {

}
