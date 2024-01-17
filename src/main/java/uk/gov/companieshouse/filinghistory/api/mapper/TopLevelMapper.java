package uk.gov.companieshouse.filinghistory.api.mapper;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class TopLevelMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(ZoneId.of("Z"));

    private final DataMapper dataMapper;
    private final OriginalValuesMapper originalValuesMapper;

    public TopLevelMapper(DataMapper dataMapper, OriginalValuesMapper originalValuesMapper) {
        this.dataMapper = dataMapper;
        this.originalValuesMapper = originalValuesMapper;
    }

    public FilingHistoryDocument mapFilingHistory(final String id, final InternalFilingHistoryApi request) {
        final InternalData internalData = request.getInternalData();
        final ExternalData externalData = request.getExternalData();

        return new FilingHistoryDocument()
                .transactionId(id)
                .entityId(internalData.getEntityId())
                .companyNumber(internalData.getCompanyNumber())
                .documentId(internalData.getDocumentId())
                .barcode(externalData.getBarcode())
                .data(dataMapper.map(externalData))
                .originalDescription(internalData.getOriginalDescription())
                .originalValues(originalValuesMapper.map(internalData.getOriginalValues()))
                .deltaAt(FORMATTER.format(internalData.getDeltaAt()))
                .updatedAt(Instant.parse(internalData.getUpdatedAt()))
                .updatedBy(internalData.getUpdatedBy());
    }

    public FilingHistoryDocument mapFilingHistory(FilingHistoryDocument document, InternalFilingHistoryApi request) {
        return null;
    }
}
