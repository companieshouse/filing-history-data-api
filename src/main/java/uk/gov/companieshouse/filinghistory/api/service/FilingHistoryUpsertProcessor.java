package uk.gov.companieshouse.filinghistory.api.service;

import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.exception.BadRequestException;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AbstractTransactionMapper;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AbstractTransactionMapperFactory;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class FilingHistoryUpsertProcessor implements UpsertProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);

    private final Service filingHistoryService;
    private final AbstractTransactionMapperFactory mapperFactory;
    private final Validator<InternalFilingHistoryApi> filingHistoryPutRequestValidator;

    public FilingHistoryUpsertProcessor(Service filingHistoryService,
                                        AbstractTransactionMapperFactory mapperFactory,
                                        Validator<InternalFilingHistoryApi> filingHistoryPutRequestValidator) {
        this.filingHistoryService = filingHistoryService;
        this.mapperFactory = mapperFactory;
        this.filingHistoryPutRequestValidator = filingHistoryPutRequestValidator;
    }

    @Override
    public void processFilingHistory(final String transactionId, final InternalFilingHistoryApi request) {
        if (!filingHistoryPutRequestValidator.isValid(request)) {
            LOGGER.error("Request body missing required field", DataMapHolder.getLogMap());
            throw new BadRequestException("Required field missing");
        }

        AbstractTransactionMapper mapper = mapperFactory.getTransactionMapper(
                request.getInternalData().getTransactionKind());

        Optional<FilingHistoryDocument> existingDocument =
                filingHistoryService.findExistingFilingHistory(transactionId);

        Optional<FilingHistoryDocument> documentToSave = existingDocument
                .map(document -> mapper.mapFilingHistoryUnlessStale(request, document))
                .orElseGet(() -> Optional.of(mapper.mapNewFilingHistory(transactionId, request)));

        if (documentToSave.isPresent()) {
            if (existingDocument.isPresent()) {
                filingHistoryService.updateFilingHistory(documentToSave.get(), existingDocument.get());
            } else {
                filingHistoryService.insertFilingHistory(documentToSave.get());
            }
        } else {
            throw new ConflictException("Stale delta for upsert");
        }
    }
}
