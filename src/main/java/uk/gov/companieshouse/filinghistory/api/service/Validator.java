package uk.gov.companieshouse.filinghistory.api.service;

public interface Validator<T> {

    ServiceResult validate(T requestBody);
}
