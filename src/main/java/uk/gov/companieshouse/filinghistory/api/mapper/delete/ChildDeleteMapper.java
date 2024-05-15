package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryChild;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@Component
public class ChildDeleteMapper {

    private final Supplier<Instant> instantSupplier;

    public ChildDeleteMapper(Supplier<Instant> instantSupplier) {
        this.instantSupplier = instantSupplier;
    }

    public <T extends FilingHistoryChild> Optional<FilingHistoryDocument> removeTransaction(String entityId, int index,
            FilingHistoryDocument documentCopy, Supplier<List<T>> childListGetter, Consumer<List<T>> childListSetter) {

        if (entityId.equals(documentCopy.getEntityId())) {
            // deleting top level: annotation, resolution (RES15) or associated filing (NEWINC)
            return Optional.empty();
        }

        List<T> childList = childListGetter.get();

        if (childList.size() == 1) {
            if (StringUtils.isBlank(documentCopy.getData().getType())) {
                // deleting document as last child and has missing parent
                return Optional.empty();
            } else {
                // deleting child array as last child and has present parent
                childListSetter.accept(null);
            }
        } else {
            // removing child from array with other child/children
            childList.remove(index);
        }

        documentCopy.updated(new FilingHistoryDeltaTimestamp()
                .at(instantSupplier.get())
                .by(DataMapHolder.getRequestId()));
        return Optional.of(documentCopy);
    }
}
