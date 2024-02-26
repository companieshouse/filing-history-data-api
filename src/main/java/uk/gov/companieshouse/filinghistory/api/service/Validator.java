package uk.gov.companieshouse.filinghistory.api.service;

public interface Validator<T> {

    boolean isValid(T requestBody);
}
