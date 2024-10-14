package uk.gov.companieshouse.filinghistory.api.service;

import static uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum.TOP_LEVEL;

import com.google.common.base.Strings;
import java.time.Instant;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryDocumentMetadataUpdateApi;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.exception.BadRequestException;
import uk.gov.companieshouse.filinghistory.api.exception.NotFoundException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AbstractTransactionMapper;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AbstractTransactionMapperFactory;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class FilingHistoryUpsertProcessor implements UpsertProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);
    private final Service filingHistoryService;
    private final AbstractTransactionMapperFactory mapperFactory;
    private final ValidatorFactory validatorFactory;
    private final Supplier<Instant> instantSupplier;

    public FilingHistoryUpsertProcessor(Service filingHistoryService,
            AbstractTransactionMapperFactory mapperFactory,
            ValidatorFactory validatorFactory,
            Supplier<Instant> instantSupplier) {
        this.filingHistoryService = filingHistoryService;
        this.mapperFactory = mapperFactory;
        this.validatorFactory = validatorFactory;
        this.instantSupplier = instantSupplier;
    }

    @Override
    public void processFilingHistory(final String transactionId,
            final String companyNumber, final InternalFilingHistoryApi request) {
        final TransactionKindEnum transactionKind = request.getInternalData().getTransactionKind();

        if (!validatorFactory.getPutRequestValidator(transactionKind).isValid(request)) {
            LOGGER.error("Request body missing required field", DataMapHolder.getLogMap());
            throw new BadRequestException("Required field missing");
        }
        Instant instant = instantSupplier.get();

        AbstractTransactionMapper mapper = mapperFactory.getTransactionMapper(transactionKind);

        filingHistoryService.findExistingFilingHistory(transactionId, companyNumber)
                .ifPresentOrElse(
                        existingDoc -> {
                            FilingHistoryDocument docToUpdate =
                                    mapper.mapExistingFilingHistory(request, existingDoc, instant);

                            LOGGER.info("Updating existing document", DataMapHolder.getLogMap());
                            filingHistoryService.updateFilingHistory(docToUpdate);
                        },
                        () -> {
                            FilingHistoryDocument newDocument = mapper.mapNewFilingHistory(transactionId, request,
                                    instant);
                            LOGGER.info("Inserting new document", DataMapHolder.getLogMap());
                            filingHistoryService.insertFilingHistory(newDocument);
                        });
    }

    @Override
    public void processDocumentMetadata(final String transactionId, final String companyNumber,
            final FilingHistoryDocumentMetadataUpdateApi request) {

        if (Strings.isNullOrEmpty(request.getDocumentMetadata())) {
            LOGGER.error("Request body missing document metadata field", DataMapHolder.getLogMap());
            throw new BadRequestException("Required field document_metadata missing");
        }
        DataMapHolder.get().filingHistoryDocumentMetadata(request.getDocumentMetadata());

        AbstractTransactionMapper mapper = mapperFactory.getTransactionMapper(TOP_LEVEL);

        filingHistoryService.findExistingFilingHistory(transactionId, companyNumber)
                .ifPresentOrElse(
                        existingDoc -> {
                            FilingHistoryDocument docToUpdate =
                                    mapper.mapDocumentMetadata(request, existingDoc);

                            filingHistoryService.updateDocumentMetadata(docToUpdate);
                        },
                        () -> {
                            throw new NotFoundException("Transaction not found for document metadata patch");
                        });
    }
}
