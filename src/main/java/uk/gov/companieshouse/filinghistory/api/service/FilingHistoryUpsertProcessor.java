package uk.gov.companieshouse.filinghistory.api.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.exception.BadRequestException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AbstractTransactionMapper;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AbstractTransactionMapperFactory;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.serdes.ObjectCopier;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class FilingHistoryUpsertProcessor implements UpsertProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);

    private final Service filingHistoryService;
    private final AbstractTransactionMapperFactory mapperFactory;
    private final ValidatorFactory validatorFactory;
    private final ObjectCopier<FilingHistoryDocument> filingHistoryDocumentCopier;

    public FilingHistoryUpsertProcessor(Service filingHistoryService,
                                        AbstractTransactionMapperFactory mapperFactory,
                                        ValidatorFactory validatorFactory,
                                        ObjectCopier<FilingHistoryDocument> filingHistoryDocumentCopier) {
        this.filingHistoryService = filingHistoryService;
        this.mapperFactory = mapperFactory;
        this.validatorFactory = validatorFactory;
        this.filingHistoryDocumentCopier = filingHistoryDocumentCopier;
    }

    @Override
    public void processFilingHistory(final String transactionId,
                                     final String companyNumber,
                                     final InternalFilingHistoryApi request) {
        final TransactionKindEnum transactionKind = request.getInternalData().getTransactionKind();

        if (!validatorFactory.getPutRequestValidator(transactionKind).isValid(request)) {
            LOGGER.error("Request body missing required field", DataMapHolder.getLogMap());
            throw new BadRequestException("Required field missing");
        }

        AbstractTransactionMapper mapper = mapperFactory.getTransactionMapper(transactionKind);

        filingHistoryService.findExistingFilingHistory(transactionId, companyNumber)
                .ifPresentOrElse(
                        existingDoc -> {
                            FilingHistoryDocument existingDocCopy = filingHistoryDocumentCopier.deepCopy(existingDoc);
                            FilingHistoryDocument docToSave = mapper.mapFilingHistoryUnlessStale(request, existingDoc);

                            filingHistoryService.updateFilingHistory(docToSave, existingDocCopy);
                        },
                        () -> {
                            FilingHistoryDocument newDocument = mapper.mapNewFilingHistory(transactionId, request);
                            filingHistoryService.insertFilingHistory(newDocument);
                        });
    }
}
