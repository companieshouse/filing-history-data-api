package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

public interface ChildListMapper<T> {

//    List<T> addNewChildToList(List<T> childList, InternalFilingHistoryApi request);
//    void updateExistingChild(T child, InternalFilingHistoryApi request);
    T mapChild(T child, InternalFilingHistoryApi request);
}
