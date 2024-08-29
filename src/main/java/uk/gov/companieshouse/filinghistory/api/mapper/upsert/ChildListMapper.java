package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;
import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.isDeltaStale;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.commons.lang.StringUtils;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryChild;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class ChildListMapper<T extends FilingHistoryChild> {

    private static final String MISSING_ENTITY_ID_ERROR_MSG =
            "Child found in MongoDB with no _entity_id; Possible duplicate being persisted.";
    private static final String STALE_DELTA_ERROR_MESSAGE =
            "Stale delta received; request delta_at: [%s] is not after existing child delta_at: [%s]";

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final ChildMapper<T> childMapper;

    public ChildListMapper(ChildMapper<T> childMapper) {
        this.childMapper = childMapper;
    }

    void mapChildList(InternalFilingHistoryApi request, List<T> existingChildList, Consumer<List<T>> childListSetter) {
        final String requestEntityId = request.getInternalData().getEntityId();

        Optional.ofNullable(existingChildList)
                .ifPresentOrElse(
                        childList -> childList.stream()
                                .filter(child -> requestEntityId.equals(child.getEntityId()))
                                .findFirst()
                                .ifPresentOrElse(child -> {
                                            if (isDeltaStale(request.getInternalData().getDeltaAt(),
                                                    child.getDeltaAt())) {
                                                LOGGER.error(STALE_DELTA_ERROR_MESSAGE.formatted(
                                                                request.getInternalData().getDeltaAt(),
                                                                child.getDeltaAt()),
                                                        DataMapHolder.getLogMap());
                                                throw new ConflictException("Stale delta when updating child");
                                            }
                                            // Update already existing child from existing list
                                            childMapper.mapChild(request, child);
                                        },
                                        // Add new child to existing list
                                        () -> {
                                            if (childList.stream()
                                                    .anyMatch(child -> StringUtils.isBlank(child.getEntityId()))) {
                                                LOGGER.info(MISSING_ENTITY_ID_ERROR_MSG, DataMapHolder.getLogMap());
                                            }
                                            childList.add(childMapper.mapChild(request));
                                        }),
                        // Add new child to a new child list
                        () -> childListSetter.accept(List.of(childMapper.mapChild(request)))
                );
    }
}
