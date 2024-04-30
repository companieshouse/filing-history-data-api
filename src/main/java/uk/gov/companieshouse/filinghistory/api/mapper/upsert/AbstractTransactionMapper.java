package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static java.time.ZoneOffset.UTC;
import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang.StringUtils;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public abstract class AbstractTransactionMapper {

    protected static final String MISSING_ENTITY_ID_ERROR_MSG =
            "Child found in MongoDB with no _entity_id; Possible duplicate being persisted with _entity_id: [%s]";
    protected static final String STALE_DELTA_ERROR_MESSAGE =
            "Stale delta received; request delta_at: [%s] is not after existing child delta_at: [%s]";

    protected static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    protected static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(UTC);

    private final LinksMapper linksMapper;

    protected AbstractTransactionMapper(LinksMapper linksMapper) {
        this.linksMapper = linksMapper;
    }

    public FilingHistoryDocument mapNewFilingHistory(String id, InternalFilingHistoryApi request,
            Instant instant) {
        FilingHistoryDocument newDocument = new FilingHistoryDocument()
                .transactionId(id)
                .created(new FilingHistoryDeltaTimestamp()
                        .at(instant)
                        .by(request.getInternalData().getUpdatedBy()))
                .data(mapFilingHistoryData(request, new FilingHistoryData())
                        .links(linksMapper.map(request.getExternalData().getLinks())));

        return mapTopLevelFields(request, newDocument, instant);
    }

    protected abstract FilingHistoryData mapFilingHistoryData(InternalFilingHistoryApi request,
            FilingHistoryData data);

    public abstract FilingHistoryDocument mapFilingHistoryToExistingDocumentUnlessStale(
            InternalFilingHistoryApi request,
            FilingHistoryDocument existingDocument, Instant instant);

    protected abstract FilingHistoryDocument mapTopLevelFields(InternalFilingHistoryApi request,
            FilingHistoryDocument document, Instant instant);

    protected static boolean isDeltaStale(final String requestDeltaAt, final String existingDeltaAt) {
        return StringUtils.isNotBlank(existingDeltaAt) && !OffsetDateTime.parse(requestDeltaAt, FORMATTER)
                .isAfter(OffsetDateTime.parse(existingDeltaAt, FORMATTER));
    }
}
