package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;

@Component
public class AbstractTransactionMapperFactory {

    private final TopLevelTransactionMapper topLevelTransactionMapper;
    private final AnnotationTransactionMapper annotationTransactionMapper;
    private final AssociatedFilingTransactionMapper associatedFilingTransactionMapper;
    private final ResolutionTransactionMapper resolutionTransactionMapper;

    public AbstractTransactionMapperFactory(TopLevelTransactionMapper topLevelTransactionMapper,
            AnnotationTransactionMapper annotationTransactionMapper,
            AssociatedFilingTransactionMapper associatedFilingTransactionMapper,
            ResolutionTransactionMapper resolutionTransactionMapper) {
        this.topLevelTransactionMapper = topLevelTransactionMapper;
        this.annotationTransactionMapper = annotationTransactionMapper;
        this.associatedFilingTransactionMapper = associatedFilingTransactionMapper;
        this.resolutionTransactionMapper = resolutionTransactionMapper;
    }

    public AbstractTransactionMapper getTransactionMapper(TransactionKindEnum kind) {
        return switch (kind) {
            case TOP_LEVEL -> topLevelTransactionMapper;
            case ANNOTATION -> annotationTransactionMapper;
            case ASSOCIATED_FILING -> associatedFilingTransactionMapper;
            case RESOLUTION -> resolutionTransactionMapper;
        };
    }
}
