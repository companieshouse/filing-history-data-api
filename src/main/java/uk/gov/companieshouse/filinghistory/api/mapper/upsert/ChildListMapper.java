package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

public interface ChildListMapper<T> {

    T mapChild(T child, InternalFilingHistoryApi request);
}
