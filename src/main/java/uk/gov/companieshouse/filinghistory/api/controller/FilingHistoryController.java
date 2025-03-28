package uk.gov.companieshouse.filinghistory.api.controller;

import static org.springframework.http.HttpHeaders.LOCATION;
import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryDocumentMetadataUpdateApi;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryList;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDeleteRequest;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryListRequestParams;
import uk.gov.companieshouse.filinghistory.api.service.DeleteProcessor;
import uk.gov.companieshouse.filinghistory.api.service.GetResponseProcessor;
import uk.gov.companieshouse.filinghistory.api.service.UpsertProcessor;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
public class FilingHistoryController {

    private static final String DEFAULT_ITEMS_PER_PAGE = "25";
    private static final int MAX_ITEMS_PER_PAGE = 100;
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

    @GetMapping("/company/{company_number}/filing-history")
    public ResponseEntity<FilingHistoryList> getCompanyFilingHistoryList(
            @PathVariable("company_number") final String companyNumber,
            @RequestParam(defaultValue = "0", name = "start_index") Integer startIndex,
            @RequestParam(defaultValue = DEFAULT_ITEMS_PER_PAGE, name = "items_per_page") Integer itemsPerPage,
            @RequestParam(required = false, name = "category") List<String> categories) {

        itemsPerPage = itemsPerPage == 0 ? Integer.parseInt(DEFAULT_ITEMS_PER_PAGE)
                : Math.min(Math.abs(itemsPerPage), MAX_ITEMS_PER_PAGE);

        DataMapHolder.get().companyNumber(companyNumber);
        LOGGER.info("Processing GET company filing history list", DataMapHolder.getLogMap());

        FilingHistoryListRequestParams requestParams = FilingHistoryListRequestParams.builder()
                .companyNumber(companyNumber)
                .startIndex(startIndex)
                .itemsPerPage(itemsPerPage)
                .categories(categories)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(filingHistoryGetResponseProcessor.processGetCompanyFilingHistoryList(requestParams));
    }

    @GetMapping("/company/{company_number}/filing-history/{transaction_id}")
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

    @PutMapping("/company/{company_number}/filing-history/{transaction_id}/internal")
    public ResponseEntity<Void> upsertFilingHistoryTransaction(
            @PathVariable("company_number") final String companyNumber,
            @PathVariable("transaction_id") final String transactionId,
            @RequestBody InternalFilingHistoryApi requestBody) {
        final String entityId = requestBody.getInternalData().getEntityId();
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .transactionId(transactionId)
                .entityId(entityId);
        LOGGER.info("Processing transaction upsert", DataMapHolder.getLogMap());

        serviceUpsertProcessor.processFilingHistory(transactionId, companyNumber, requestBody);

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(LOCATION, "/company/%s/filing-history/%s".formatted(companyNumber, transactionId))
                .build();
    }

    @DeleteMapping("/company/{company_number}/filing-history/{transaction_id}/internal")
    public ResponseEntity<Void> deleteFilingHistoryTransaction(
            @PathVariable("company_number") final String companyNumber,
            @PathVariable("transaction_id") final String transactionId,
            @RequestHeader("X-DELTA-AT") final String deltaAt,
            @RequestHeader("X-ENTITY-ID") final String entityId,
            @RequestHeader(value = "X-PARENT-ENTITY-ID", defaultValue = "") final String parentEntityId) {

        DataMapHolder.get()
                .companyNumber(companyNumber)
                .entityId(entityId)
                .transactionId(transactionId);
        LOGGER.info("Processing transaction delete", DataMapHolder.getLogMap());

        FilingHistoryDeleteRequest request = FilingHistoryDeleteRequest.builder()
                .companyNumber(companyNumber)
                .transactionId(transactionId)
                .entityId(entityId)
                .deltaAt(deltaAt)
                .parentEntityId(parentEntityId)
                .build();

        serviceDeleteProcessor.processFilingHistoryDelete(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @PatchMapping("/company/{company_number}/filing-history/{transaction_id}/document-metadata")
    public ResponseEntity<Void> patchFilingHistoryDocumentMetadata(
            @PathVariable("company_number") final String companyNumber,
            @PathVariable("transaction_id") final String transactionId,
            @RequestBody FilingHistoryDocumentMetadataUpdateApi requestBody) {

        DataMapHolder.get()
                .companyNumber(companyNumber)
                .transactionId(transactionId);
        LOGGER.info("Processing document metadata patch", DataMapHolder.getLogMap());

        serviceUpsertProcessor.processDocumentMetadata(transactionId, companyNumber, requestBody);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
