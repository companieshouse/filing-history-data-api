package uk.gov.companieshouse.filinghistory.api.service;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.mapper.TopLevelTransactionMapper;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.ServiceResult;

@Component
public class FilingHistoryProcessor implements Processor {

    private final FilingHistoryService filingHistoryService;
    private final TopLevelTransactionMapper topLevelMapper;

    public FilingHistoryProcessor(FilingHistoryService filingHistoryService, TopLevelTransactionMapper topLevelMapper) {
        this.filingHistoryService = filingHistoryService;
        this.topLevelMapper = topLevelMapper;
    }

    @Override
    public ServiceResult processFilingHistory(final String transactionId, final InternalFilingHistoryApi request) {
        Optional<FilingHistoryDocument> documentToSave = filingHistoryService.findExistingFilingHistory(transactionId)
                .map(existingDocument -> topLevelMapper.mapFilingHistoryUnlessStale(request, existingDocument))
                .orElseGet(() -> Optional.of(topLevelMapper.mapNewFilingHistory(transactionId, request)));

        return documentToSave
                .map(filingHistoryService::saveFilingHistory)
                .orElse(ServiceResult.STALE_DELTA);
    }
}
