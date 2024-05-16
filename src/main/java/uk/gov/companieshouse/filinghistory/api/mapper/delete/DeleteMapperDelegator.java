package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.exception.BadRequestException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeleteAggregate;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.serdes.FilingHistoryDocumentCopier;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class DeleteMapperDelegator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);
    private static final String COMPOSITE_RES_TYPE = "RESOLUTIONS";
    private final CompositeResolutionDeleteMapper compositeResolutionDeleteMapper;
    private final FilingHistoryDocumentCopier documentCopier;

    public DeleteMapperDelegator(CompositeResolutionDeleteMapper compositeResolutionDeleteMapper,
            FilingHistoryDocumentCopier documentCopier) {
        this.compositeResolutionDeleteMapper = compositeResolutionDeleteMapper;
        this.documentCopier = documentCopier;
    }

    @DeleteChildTransactions
    public Optional<FilingHistoryDocument> delegateDelete(String entityId, FilingHistoryDeleteAggregate aggregate) {
        FilingHistoryDocument document = documentCopier.deepCopy(aggregate.getDocument());

        final int resIndex = aggregate.getResolutionIndex();
        if (resIndex >= 0) {
            if (COMPOSITE_RES_TYPE.equals(document.getData().getType())) {
                LOGGER.debug("Matched composite resolution _entity_id: [%s]".formatted(entityId),
                        DataMapHolder.getLogMap());
                return compositeResolutionDeleteMapper.removeTransaction(resIndex, document);
            } else {
                LOGGER.debug("Matched child resolution _entity_id: [%s]".formatted(entityId),
                        DataMapHolder.getLogMap());
                throw new BadRequestException("No mapper for child resolutions");
            }
        }

        if (entityId.equals(document.getEntityId())) {
            LOGGER.debug("Matched top level _entity_id: [%s]".formatted(entityId), DataMapHolder.getLogMap());
            return Optional.empty();
        } else {
            LOGGER.debug("No match for _entity_id: [%s]".formatted(entityId), DataMapHolder.getLogMap());
            throw new BadRequestException("No match for _entity_id: [%s]".formatted(entityId));
        }
    }
}
