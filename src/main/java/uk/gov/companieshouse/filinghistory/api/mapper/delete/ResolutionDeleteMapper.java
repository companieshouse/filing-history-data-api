package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

public class ResolutionDeleteMapper implements DeleteMapper {

    private RemoveChildMapper removeChildMapper;

    @Override
    public FilingHistoryDocument deleteChild(FilingHistoryDocument existingDocument) {
        return null;
    }
}
