package uk.gov.companieshouse.filinghistory.api.controller;

import static org.springframework.http.HttpHeaders.LOCATION;
import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.ServiceResult;
import uk.gov.companieshouse.filinghistory.api.service.Processor;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
public class FilingHistoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final Processor serviceProcessor;

    public FilingHistoryController(Processor serviceProcessor) {
        this.serviceProcessor = serviceProcessor;
    }

    @PutMapping("/company/{company_number}/filing-history/{transaction_id}")
    public ResponseEntity<Void> upsertFilingHistoryTransaction(
            @PathVariable("company_number") final String companyNumber,
            @PathVariable("transaction_id") final String transactionId,
            @RequestBody InternalFilingHistoryApi requestBody) {

        DataMapHolder.get()
                .companyNumber(companyNumber)
                .transactionId(transactionId);
        LOGGER.info("Processing transaction upsert", DataMapHolder.getLogMap());

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
