package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.isDeltaStale;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class CompositeResolutionDeleteMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);

    private final Supplier<Instant> instantSupplier;

    public CompositeResolutionDeleteMapper(Supplier<Instant> instantSupplier) {
        this.instantSupplier = instantSupplier;
    }

    public Optional<FilingHistoryDocument> removeTransaction(int index, String requestDeltaAt,
            FilingHistoryDocument documentCopy) {
        List<FilingHistoryResolution> resolutions = documentCopy.getData().getResolutions();
        String deltaAt = resolutions.get(index).getDeltaAt();

        if (isDeltaStale(requestDeltaAt, deltaAt)) {
            LOGGER.error("Stale delta received; request delta_at: [%s] is not after existing delta_at: [%s]".formatted(
                    requestDeltaAt, deltaAt), DataMapHolder.getLogMap());
            throw new ConflictException("Stale delta for composite resolution delete");
        }

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
