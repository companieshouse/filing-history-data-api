package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

@Component
public class CompositeResolutionDeleteMapper {

    private final Supplier<Instant> instantSupplier;

    public CompositeResolutionDeleteMapper(Supplier<Instant> instantSupplier) {
        this.instantSupplier = instantSupplier;
    }

    public Optional<FilingHistoryDocument> removeTransaction(int index, FilingHistoryDocument documentCopy) {
        List<FilingHistoryResolution> resolutions = documentCopy.getData().getResolutions();

        if (resolutions.size() == 1) {
            return Optional.empty();
        } else {
            resolutions.remove(index);
            documentCopy.updated(new FilingHistoryDeltaTimestamp()
                    .at(instantSupplier.get())
                    .by(DataMapHolder.getRequestId()));
            return Optional.of(documentCopy);
        }
    }
}
