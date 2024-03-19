package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class AbstractTransactionMapperFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final TopLevelTransactionMapper topLevelTransactionMapper;
    private final AnnotationTransactionMapper annotationTransactionMapper;
    private final AssociatedFilingTransactionMapper associatedFilingTransactionMapper;

    public AbstractTransactionMapperFactory(TopLevelTransactionMapper topLevelTransactionMapper, AnnotationTransactionMapper annotationTransactionMapper, AssociatedFilingTransactionMapper associatedFilingTransactionMapper) {
        this.topLevelTransactionMapper = topLevelTransactionMapper;
        this.annotationTransactionMapper = annotationTransactionMapper;
        this.associatedFilingTransactionMapper = associatedFilingTransactionMapper;
    }

    public AbstractTransactionMapper getTransactionMapper(TransactionKindEnum kind) {
        LOGGER.debug("Getting mapper for [%s] transaction kind".formatted(kind.getValue()), DataMapHolder.getLogMap());
        return switch (kind) {
            case TOP_LEVEL -> topLevelTransactionMapper;
            case ANNOTATION -> annotationTransactionMapper;
            case ASSOCIATED_FILING -> associatedFilingTransactionMapper;
            case RESOLUTION -> {
                LOGGER.error("Invalid transaction kind: %s".formatted(kind.getValue()));
                throw new InvalidTransactionKindException("Invalid transaction kind: %s".formatted(kind.getValue()));
            }
        };
    }
}
