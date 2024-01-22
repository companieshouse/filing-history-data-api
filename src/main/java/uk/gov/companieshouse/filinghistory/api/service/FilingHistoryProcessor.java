package uk.gov.companieshouse.filinghistory.api.service;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.mapper.AbstractTransactionMapper;
import uk.gov.companieshouse.filinghistory.api.mapper.AbstractTransactionMapperFactory;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@Component
public class FilingHistoryProcessor implements Processor {

    private final FilingHistoryService filingHistoryService;
    private final AbstractTransactionMapperFactory mapperFactory;

    public FilingHistoryProcessor(FilingHistoryService filingHistoryService,
            AbstractTransactionMapperFactory mapperFactory) {
        this.filingHistoryService = filingHistoryService;
        this.mapperFactory = mapperFactory;
    }

    @Override
    public ServiceResult processFilingHistory(final String transactionId, final InternalFilingHistoryApi request) {
        AbstractTransactionMapper mapper = mapperFactory.getTransactionMapper(
                request.getInternalData().getTransactionKind());

        Optional<FilingHistoryDocument> documentToSave = filingHistoryService.findExistingFilingHistory(transactionId)
                .map(existingDocument -> mapper.mapFilingHistoryUnlessStale(request, existingDocument))
                .orElseGet(() -> Optional.of(mapper.mapNewFilingHistory(transactionId, request)));

        return documentToSave
                .map(filingHistoryService::saveFilingHistory)
                .orElse(ServiceResult.STALE_DELTA);
    }
}
