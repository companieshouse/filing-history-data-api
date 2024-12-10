package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.isDeltaStale;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryChild;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class ChildDeleteMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);

    private final Supplier<Instant> instantSupplier;

    public ChildDeleteMapper(Supplier<Instant> instantSupplier) {
        this.instantSupplier = instantSupplier;
    }

    public <T extends FilingHistoryChild> Optional<FilingHistoryDocument> removeTransaction(String entityId,
            String requestDeltaAt, int index, FilingHistoryDocument documentCopy, Supplier<List<T>> childListGetter,
            Function<List<T>, FilingHistoryData> childListSetter) {
        List<T> childList = childListGetter.get();
        String deltaAt = childList.get(index).getDeltaAt();

        if (isDeltaStale(requestDeltaAt, deltaAt)) {
            LOGGER.error("Stale delta received; request delta_at: [%s] is not after existing delta_at: [%s]".formatted(
                    requestDeltaAt, deltaAt), DataMapHolder.getLogMap());
            throw new ConflictException("Stale delta for child delete");
        }

        if (entityId.equals(documentCopy.getEntityId())) {
            return Optional.empty();
        }

        if (childList.size() == 1) {
            if (StringUtils.isBlank(documentCopy.getData().getType())) {
                return Optional.empty();
            } else {
                childListSetter.apply(null);
            }
        } else {
            childList.remove(index);
        }

        documentCopy.updated(new FilingHistoryDeltaTimestamp()
                .at(instantSupplier.get())
                .by(DataMapHolder.getRequestId()));
        return Optional.of(documentCopy);
    }
}
