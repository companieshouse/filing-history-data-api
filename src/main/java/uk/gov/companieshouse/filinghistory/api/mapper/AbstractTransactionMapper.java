package uk.gov.companieshouse.filinghistory.api.mapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

public abstract class AbstractTransactionMapper {

    protected static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(ZoneOffset.UTC);

    public FilingHistoryDocument mapNewFilingHistory(final String id, final InternalFilingHistoryApi request) {
        return mapFilingHistory(id, request, null);
    }

    protected abstract Optional<FilingHistoryDocument> mapFilingHistoryUnlessStale(InternalFilingHistoryApi request,
            FilingHistoryDocument existingDocument);

    protected abstract FilingHistoryDocument mapFilingHistory(String id, InternalFilingHistoryApi request,
            FilingHistoryDocument existingDocument);

    protected static boolean isDeltaStale(final String requestDeltaAt, final String existingDeltaAt) {
        return !OffsetDateTime.parse(requestDeltaAt, FORMATTER)
                .isAfter(OffsetDateTime.parse(existingDeltaAt, FORMATTER));
    }
}
