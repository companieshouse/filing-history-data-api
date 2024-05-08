package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

public interface DeleteMapper {

    FilingHistoryDocument deleteChild(FilingHistoryDocument existingDocument);

}
