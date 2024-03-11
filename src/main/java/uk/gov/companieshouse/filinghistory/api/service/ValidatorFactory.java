package uk.gov.companieshouse.filinghistory.api.service;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.InvalidTransactionKindException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class ValidatorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final TopLevelPutRequestValidator topLevelPutRequestValidator;
    private final AnnotationPutRequestValidator annotationPutRequestValidator;

    public ValidatorFactory(TopLevelPutRequestValidator topLevelPutRequestValidator, AnnotationPutRequestValidator annotationPutRequestValidator) {
        this.topLevelPutRequestValidator = topLevelPutRequestValidator;
        this.annotationPutRequestValidator = annotationPutRequestValidator;
    }

    public Validator<InternalFilingHistoryApi> getPutRequestValidator(TransactionKindEnum kind) {
        LOGGER.debug("Getting validator for [%s] transaction kind".formatted(kind.getValue()), DataMapHolder.getLogMap());
        return switch (kind) {
            case TOP_LEVEL -> topLevelPutRequestValidator;
            case ANNOTATION -> annotationPutRequestValidator;
            case RESOLUTION, ASSOCIATED_FILING -> {
                LOGGER.error("Invalid transaction kind: %s".formatted(kind.getValue()));
                throw new InvalidTransactionKindException("Invalid transaction kind: %s".formatted(kind.getValue()));
            }
        };
    }
}
