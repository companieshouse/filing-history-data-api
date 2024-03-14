package uk.gov.companieshouse.filinghistory.api.controller;

import static org.springframework.http.HttpHeaders.LOCATION;
import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryList;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.service.DeleteProcessor;
import uk.gov.companieshouse.filinghistory.api.service.GetResponseProcessor;
import uk.gov.companieshouse.filinghistory.api.service.UpsertProcessor;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
public class FilingHistoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final GetResponseProcessor filingHistoryGetResponseProcessor;
    private final UpsertProcessor serviceUpsertProcessor;
    private final DeleteProcessor serviceDeleteProcessor;

    public FilingHistoryController(GetResponseProcessor filingHistoryGetResponseProcessor,
            UpsertProcessor serviceUpsertProcessor, DeleteProcessor serviceDeleteProcessor) {
        this.filingHistoryGetResponseProcessor = filingHistoryGetResponseProcessor;
        this.serviceUpsertProcessor = serviceUpsertProcessor;
        this.serviceDeleteProcessor = serviceDeleteProcessor;
    }

    @GetMapping("/filing-history-data-api/company/{company_number}/filing-history")
    public ResponseEntity<FilingHistoryList> getCompanyFilingHistoryList(
            @PathVariable("company_number") final String companyNumber) {

        DataMapHolder.get()
                .companyNumber(companyNumber);
        LOGGER.info("Processing GET company filing history list", DataMapHolder.getLogMap());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(filingHistoryGetResponseProcessor.processGetCompanyFilingHistoryList(companyNumber));
    }

    @GetMapping("/filing-history-data-api/company/{company_number}/filing-history/{transaction_id}")
    public ResponseEntity<ExternalData> getSingleFilingHistory(
            @PathVariable("company_number") final String companyNumber,
            @PathVariable("transaction_id") final String transactionId) {

        DataMapHolder.get()
                .companyNumber(companyNumber)
                .transactionId(transactionId);
        LOGGER.info("Processing GET single transaction", DataMapHolder.getLogMap());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(filingHistoryGetResponseProcessor.processGetSingleFilingHistory(transactionId, companyNumber));
    }

    @PutMapping("/filing-history-data-api/company/{company_number}/filing-history/{transaction_id}/internal")
    public ResponseEntity<Void> upsertFilingHistoryTransaction(
            @PathVariable("company_number") final String companyNumber,
            @PathVariable("transaction_id") final String transactionId,
            @RequestBody InternalFilingHistoryApi requestBody) {

        DataMapHolder.get()
                .companyNumber(companyNumber)
                .transactionId(transactionId);
        LOGGER.info("Processing transaction upsert", DataMapHolder.getLogMap());

        serviceUpsertProcessor.processFilingHistory(transactionId, companyNumber, requestBody);

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(LOCATION, "/company/%s/filing-history/%s".formatted(companyNumber, transactionId))
                .build();
    }

    @DeleteMapping("/filing-history-data-api/filing-history/{transaction_id}/internal")
    public ResponseEntity<Void> deleteFilingHistoryTransaction(
            @PathVariable("transaction_id") final String transactionId) {

        DataMapHolder.get()
                .transactionId(transactionId);
        LOGGER.info("Processing transaction delete", DataMapHolder.getLogMap());

        serviceDeleteProcessor.processFilingHistoryDelete(transactionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
