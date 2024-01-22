package uk.gov.companieshouse.filinghistory.api.mapper;


import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class AbstractTransactionMapperFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final TopLevelTransactionMapper topLevelTransactionMapper;

    public AbstractTransactionMapperFactory(TopLevelTransactionMapper topLevelTransactionMapper) {
        this.topLevelTransactionMapper = topLevelTransactionMapper;
    }

    public AbstractTransactionMapper getTransactionMapper(TransactionKindEnum kind) {
        LOGGER.debug("Getting mapper for [%s] transaction kind".formatted(kind.getValue()), DataMapHolder.getLogMap());
        return switch (kind) {
            case TOP_LEVEL -> topLevelTransactionMapper;
            case ANNOTATION, RESOLUTION, ASSOCIATED_FILING -> {
                LOGGER.error("Invalid transaction kind: %s".formatted(kind.getValue()));
                throw new InvalidMapperException("Invalid transaction kind: %s".formatted(kind.getValue()));
            }
        };
    }

}
