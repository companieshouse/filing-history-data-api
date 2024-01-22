package uk.gov.companieshouse.filinghistory.api.controller;

import static org.springframework.http.HttpHeaders.LOCATION;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.ServiceResult;
import uk.gov.companieshouse.filinghistory.api.service.Processor;

@RestController
public class FilingHistoryController {

    private final Processor serviceProcessor;

    public FilingHistoryController(Processor serviceProcessor) {
        this.serviceProcessor = serviceProcessor;
    }

    @PutMapping("/company/{company_number}/filing-history/{transaction_id}")
    public ResponseEntity<Void> upsertFilingHistoryTransaction(
            @PathVariable("company_number") final String companyNumber,
            @PathVariable("transaction_id") final String transactionId,
            @RequestBody final InternalFilingHistoryApi requestBody) {

        final ServiceResult result = serviceProcessor.processFilingHistory(transactionId, requestBody);

        // This is a switch because we'll need to add more cases in the future when doing unhappy paths
        final HttpStatus status = switch (result) {
            case STALE_DELTA -> HttpStatus.CONFLICT;
            default -> HttpStatus.OK;
        };
        return ResponseEntity
                .status(status)
                .header(LOCATION, "/company/%s/filing-history/%s".formatted(companyNumber, transactionId))
                .build();
    }
}
