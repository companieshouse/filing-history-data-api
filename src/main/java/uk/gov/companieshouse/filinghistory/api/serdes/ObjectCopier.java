package uk.gov.companieshouse.filinghistory.api.serdes;

public interface ObjectCopier<T> {

    T deepCopy(T originalObject);
}
