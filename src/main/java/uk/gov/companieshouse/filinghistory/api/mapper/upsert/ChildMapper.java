package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryChild;

public interface ChildMapper<T extends FilingHistoryChild> {

    T mapChild(InternalFilingHistoryApi request, T child);

    T newInstance();

    default T mapChild(InternalFilingHistoryApi request) {
        return mapChild(request, newInstance());
    }
}
