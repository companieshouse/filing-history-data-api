package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import java.util.Optional;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Aspect
@Component
@ConditionalOnProperty(prefix = "feature", name = "delete_child_transactions.disabled")
public class DeleteMapperDelegatorAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);

    @Around("@annotation(DeleteChildTransactions)")
    public Optional<FilingHistoryDocument> deleteChildTransactionsDisabled() {
        LOGGER.debug("Deletion of child transactions disabled", DataMapHolder.getLogMap());
        return Optional.empty();
    }
}
