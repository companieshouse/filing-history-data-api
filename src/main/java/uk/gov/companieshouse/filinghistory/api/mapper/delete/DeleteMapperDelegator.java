package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.exception.InternalServerErrorException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryChild;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class DeleteMapperDelegator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);
    private final CompositeResolutionDeleteMapper compositeResolutionDeleteMapper;

    public DeleteMapperDelegator(CompositeResolutionDeleteMapper compositeResolutionDeleteMapper) {
        this.compositeResolutionDeleteMapper = compositeResolutionDeleteMapper;
    }

    public Optional<FilingHistoryDocument> delegateDelete(String entityId, FilingHistoryDocument document) {
        FilingHistoryData data = document.getData();

        final int resIndex = getMatchedIndex(entityId, data.getResolutions());
        if (resIndex >= 0) {
            if ("RESOLUTIONS".equals(data.getType())) {
                LOGGER.debug("Matched composite resolution _entity_id: [%s]".formatted(entityId),
                        DataMapHolder.getLogMap());
                return compositeResolutionDeleteMapper.removeTransaction(resIndex, document);
            } else {
                LOGGER.debug("Matched child resolution _entity_id: [%s]".formatted(entityId),
                        DataMapHolder.getLogMap());
                throw new InternalServerErrorException("No mapper for child resolutions");
            }
        }

        if (entityId.equals(document.getEntityId())) {
            LOGGER.debug("Matched top level _entity_id: [%s]".formatted(entityId), DataMapHolder.getLogMap());
            return Optional.empty();
        } else {
            LOGGER.debug("No match for _entity_id: [%s]".formatted(entityId), DataMapHolder.getLogMap());
            throw new InternalServerErrorException("No match for _entity_id: [%s]".formatted(entityId));
        }
    }

    private static <T extends FilingHistoryChild> int getMatchedIndex(String entityId, List<T> transactionList) {
        if (transactionList != null) {
            long matchedIndex = transactionList.stream()
                    .takeWhile(res -> !entityId.equals(res.getEntityId()))
                    .count();
            if (matchedIndex < transactionList.size()) {
                return Math.toIntExact(matchedIndex);
            }
        }
        return -1;
    }
}
