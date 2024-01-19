package uk.gov.companieshouse.filinghistory.api.mapper;


import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;

public class AbstractTransactionMapperFactory {

    private final TopLevelTransactionMapper topLevelTransactionMapper;

    public AbstractTransactionMapperFactory(TopLevelTransactionMapper topLevelTransactionMapper) {
        this.topLevelTransactionMapper = topLevelTransactionMapper;
    }

    public AbstractTransactionMapper getTransactionMapper(TransactionKindEnum kind) {
        return switch (kind) {
            case TOP_LEVEL -> topLevelTransactionMapper;
            case ANNOTATION, RESOLUTION, ASSOCIATED_FILING ->
                    throw new InvalidMapperException("Unknown transaction kind: %s".formatted(kind.getValue()));
        };
    }

}
