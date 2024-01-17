package uk.gov.companieshouse.filinghistory.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.ServiceResult;
import uk.gov.companieshouse.filinghistory.api.service.Service;

@RestController
public class FilingHistoryController {

    private final Service filingHistoryService;

    public FilingHistoryController(Service filingHistoryService) {
        this.filingHistoryService = filingHistoryService;
    }

    @PutMapping("/company/{company_number}/filing-history/{transaction_id}")
    public ResponseEntity<Void> upsertFilingHistoryTransaction(
            @PathVariable("company_number") final String companyNumber,
            @PathVariable("transaction_id") final String transactionId,
            @RequestBody final InternalFilingHistoryApi requestBody) {

        final HttpStatus status;
        final ServiceResult result = filingHistoryService.upsertFilingHistory(transactionId, requestBody);

        // This is a switch because we'll need to add more cases in the future when doing unhappy paths
        switch (result) {
            case STALE_DELTA -> status = HttpStatus.CONFLICT;
            default -> status = HttpStatus.OK;
        }
        return ResponseEntity.status(status).build();
    }
}
