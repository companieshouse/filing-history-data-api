package uk.gov.companieshouse.filinghistory.api.service;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.NotFoundException;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AbstractTransactionMapper;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AbstractTransactionMapperFactory;
import uk.gov.companieshouse.filinghistory.api.mapper.get.ItemGetResponseMapper;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@Component
public class FilingHistoryProcessor implements Processor {

    private final Service filingHistoryService;
    private final AbstractTransactionMapperFactory mapperFactory;
    private final ItemGetResponseMapper itemGetResponseMapper;

    public FilingHistoryProcessor(Service filingHistoryService,
                                  AbstractTransactionMapperFactory mapperFactory, ItemGetResponseMapper itemGetResponseMapper) {
        this.filingHistoryService = filingHistoryService;
        this.mapperFactory = mapperFactory;
        this.itemGetResponseMapper = itemGetResponseMapper;
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

    @Override
    public ExternalData processGetSingleFilingHistory(final String companyNumber, final String transactionId) {
        return itemGetResponseMapper.mapFilingHistoryItem(
                filingHistoryService.findExistingFilingHistory(transactionId)
                        .orElseThrow(() ->
                                new NotFoundException("Record with transaction id: %s could not be found in MongoDB"
                                        .formatted(transactionId))));
    }
}
