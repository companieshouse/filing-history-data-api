package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

@Component
public class ResolutionChildMapper implements ChildMapper<FilingHistoryResolution> {

    @Override
    public FilingHistoryResolution mapChild(FilingHistoryResolution child, InternalFilingHistoryApi request) {
        return null;
    }
}
