package uk.gov.companieshouse.filinghistory.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class BadGatewayException extends RuntimeException {

    public BadGatewayException(String message) {
        super(message);
    }

    public BadGatewayException(String message, Throwable ex) {
        super(message, ex);
    }
}
