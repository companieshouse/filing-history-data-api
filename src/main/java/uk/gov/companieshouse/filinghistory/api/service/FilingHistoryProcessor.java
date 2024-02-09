package uk.gov.companieshouse.filinghistory.api.service;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.mapper.AbstractTransactionMapper;
import uk.gov.companieshouse.filinghistory.api.mapper.AbstractTransactionMapperFactory;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@Component
public class FilingHistoryProcessor implements Processor {

    private final Service filingHistoryService;
    private final AbstractTransactionMapperFactory mapperFactory;

    public FilingHistoryProcessor(Service filingHistoryService,
            AbstractTransactionMapperFactory mapperFactory) {
        this.filingHistoryService = filingHistoryService;
        this.mapperFactory = mapperFactory;
    }

    @Override
    public ServiceResult processFilingHistory(final String transactionId, final InternalFilingHistoryApi request) {
        AbstractTransactionMapper mapper = mapperFactory.getTransactionMapper(
                request.getInternalData().getTransactionKind());

        Optional<FilingHistoryDocument> existingDocument = filingHistoryService.findExistingFilingHistory(transactionId);

        Optional<FilingHistoryDocument> documentToSave = existingDocument
                .map(document -> mapper.mapFilingHistoryUnlessStale(request, document))
                .orElseGet(() -> Optional.of(mapper.mapNewFilingHistory(transactionId, request)));

        return documentToSave
                .map(document -> existingDocument
                        .map(existingDoc -> filingHistoryService.updateFilingHistory(document, existingDoc))
                        .orElseGet(() -> filingHistoryService.insertFilingHistory(document)))
                .orElse(ServiceResult.STALE_DELTA);
    }
}
