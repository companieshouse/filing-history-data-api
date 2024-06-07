package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import java.util.Optional;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.exception.BadRequestException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeleteAggregate;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Aspect
@Component
@ConditionalOnProperty(prefix = "feature", name = "delete_child_transactions.disabled", havingValue = "true")
public class DeleteMapperDelegatorAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);

    @Around("@annotation(DeleteChildTransactions)")
    public Optional<FilingHistoryDocument> deleteChildTransactionsDisabled(JoinPoint joinPoint) {
        LOGGER.debug("Deletion of child transactions disabled", DataMapHolder.getLogMap());
        Object[] args = joinPoint.getArgs();
        String entityId = (String) args[0];
        FilingHistoryDeleteAggregate aggregate = (FilingHistoryDeleteAggregate) args[1];

        if (!entityId.equals(aggregate.getDocument().getEntityId())
                || "RESOLUTIONS".equals(aggregate.getDocument().getData().getType())) {
            LOGGER.error("Cannot delete child while child deletion disabled, _entity_id: [%s]"
                    .formatted(entityId), DataMapHolder.getLogMap());
            throw new BadRequestException("Cannot delete child while child deletion disabled, _entity_id: [%s]"
                    .formatted(entityId));
        } else {
            LOGGER.debug("Matched parent _entity_id: [%s]".formatted(entityId), DataMapHolder.getLogMap());
            return Optional.empty();
        }
    }
}
