package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

public class InvalidTransactionKindException extends RuntimeException {
    public InvalidTransactionKindException(String message) {
        super(message);
    }
}
