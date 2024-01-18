package uk.gov.companieshouse.filinghistory.api.mapper;

import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@Component
public class TopLevelMapper extends AbstractMapper {

    private final DataMapper dataMapper;
    private final OriginalValuesMapper originalValuesMapper;

    public TopLevelMapper(DataMapper dataMapper, OriginalValuesMapper originalValuesMapper) {
        this.dataMapper = dataMapper;
        this.originalValuesMapper = originalValuesMapper;
    }

    @Override
    public Optional<FilingHistoryDocument> mapFilingHistoryUnlessStale(InternalFilingHistoryApi request,
            FilingHistoryDocument existingDocument) {
        if (isDeltaStale(request.getInternalData().getDeltaAt(), existingDocument.getDeltaAt())) {
            return Optional.empty();
        }
        return Optional.of(mapFilingHistory(null, request, existingDocument));
    }

    @Override
    protected FilingHistoryDocument mapFilingHistory(String id, InternalFilingHistoryApi request,
            FilingHistoryDocument existingDocument) {
        final InternalData internalData = request.getInternalData();
        final ExternalData externalData = request.getExternalData();

        return Optional.ofNullable(existingDocument)
                .map(document -> existingDocument)
                .orElse(new FilingHistoryDocument()
                        .transactionId(id))
                .entityId(internalData.getEntityId())
                .companyNumber(internalData.getCompanyNumber())
                .documentId(internalData.getDocumentId())
                .barcode(externalData.getBarcode())
                .data(dataMapper.mapFilingHistoryExternalData(externalData))
                .originalDescription(internalData.getOriginalDescription())
                .originalValues(originalValuesMapper.map(internalData.getOriginalValues()))
                .deltaAt(FORMATTER.format(internalData.getDeltaAt()))
                .updatedAt(Instant.parse(internalData.getUpdatedAt()))
                .updatedBy(internalData.getUpdatedBy());
    }
}
