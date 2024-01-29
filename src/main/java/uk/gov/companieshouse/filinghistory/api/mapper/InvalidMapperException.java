package uk.gov.companieshouse.filinghistory.api.mapper;

public class InvalidMapperException extends RuntimeException {
    public InvalidMapperException(String message) {
        super(message);
    }
}
