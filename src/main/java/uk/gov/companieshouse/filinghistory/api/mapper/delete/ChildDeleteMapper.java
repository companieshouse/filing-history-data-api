package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryChild;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@Component
public class ChildDeleteMapper {

    private final Supplier<Instant> instantSupplier;

    public ChildDeleteMapper(Supplier<Instant> instantSupplier) {
        this.instantSupplier = instantSupplier;
    }

    public <T extends FilingHistoryChild> Optional<FilingHistoryDocument> removeTransaction(String entityId, int index,
            FilingHistoryDocument documentCopy, Supplier<List<T>> childListGetter,
            Function<List<T>, FilingHistoryData> childListSetter) {

        if (entityId.equals(documentCopy.getEntityId())) {
            return Optional.empty();
        }

        List<T> childList = childListGetter.get();

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
