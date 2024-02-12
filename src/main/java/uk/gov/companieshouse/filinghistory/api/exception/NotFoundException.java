package uk.gov.companieshouse.filinghistory.api.exception;

public class NotFoundException extends RuntimeException{

    public NotFoundException(String message) {
        super(message);
    }
}
