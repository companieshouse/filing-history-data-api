package uk.gov.companieshouse.filinghistory.api.service;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AbstractTransactionMapper;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AbstractTransactionMapperFactory;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@Component
public class FilingHistoryUpsertProcessor implements UpsertProcessor {

    private final Service filingHistoryService;
    private final AbstractTransactionMapperFactory mapperFactory;

    public FilingHistoryUpsertProcessor(Service filingHistoryService,
                                        AbstractTransactionMapperFactory mapperFactory) {
        this.filingHistoryService = filingHistoryService;
        this.mapperFactory = mapperFactory;
    }

    @Override
    public ServiceResult processFilingHistory(final String transactionId, final InternalFilingHistoryApi request) {
        AbstractTransactionMapper mapper = mapperFactory.getTransactionMapper(
                request.getInternalData().getTransactionKind());

        Optional<FilingHistoryDocument> existingDocument;
        try {
            existingDocument = filingHistoryService.findExistingFilingHistory(transactionId);
        } catch (ServiceUnavailableException ex) { // TODO: Could I catch a DataAccessException here or should I throw the service unavailable in the repository class?
            return ServiceResult.SERVICE_UNAVAILABLE;
        }

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
