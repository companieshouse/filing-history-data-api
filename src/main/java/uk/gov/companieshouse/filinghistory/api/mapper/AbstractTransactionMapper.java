package uk.gov.companieshouse.filinghistory.api.mapper;

import static java.time.ZoneOffset.UTC;
import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public abstract class AbstractTransactionMapper {

    protected static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    protected static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(UTC);

    private final LinksMapper linksMapper;

    protected AbstractTransactionMapper(LinksMapper linksMapper) {
        this.linksMapper = linksMapper;
    }

    public FilingHistoryDocument mapNewFilingHistory(String id, InternalFilingHistoryApi request) {
        FilingHistoryDocument newDocument = new FilingHistoryDocument()
                .transactionId(id)
                .data(mapFilingHistoryData(request.getExternalData(), new FilingHistoryData())
                        .links(linksMapper.map(request.getExternalData().getLinks())));

        return mapFilingHistory(request, newDocument);
    }

    protected abstract FilingHistoryData mapFilingHistoryData(ExternalData externalData,
            FilingHistoryData existingData);

    protected abstract Optional<FilingHistoryDocument> mapFilingHistoryUnlessStale(InternalFilingHistoryApi request,
            FilingHistoryDocument existingDocument);

    protected abstract FilingHistoryDocument mapFilingHistory(InternalFilingHistoryApi request,
            FilingHistoryDocument document);

    protected static boolean isDeltaStale(final String requestDeltaAt, final String existingDeltaAt) {
        return !OffsetDateTime.parse(requestDeltaAt, FORMATTER)
                .isAfter(OffsetDateTime.parse(existingDeltaAt, FORMATTER));
    }
}