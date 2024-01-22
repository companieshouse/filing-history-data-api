package uk.gov.companieshouse.filinghistory.api.mapper;

import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@Component
public class TopLevelTransactionMapper extends AbstractTransactionMapper {

    private final DataMapper dataMapper;
    private final OriginalValuesMapper originalValuesMapper;

    public TopLevelTransactionMapper(DataMapper dataMapper, OriginalValuesMapper originalValuesMapper,
            LinksMapper linksMapper) {
        super(linksMapper);
        this.dataMapper = dataMapper;
        this.originalValuesMapper = originalValuesMapper;
    }

    @Override
    public Optional<FilingHistoryDocument> mapFilingHistoryUnlessStale(InternalFilingHistoryApi request,
            FilingHistoryDocument existingDocument) {
        if (isDeltaStale(request.getInternalData().getDeltaAt(), existingDocument.getDeltaAt())) {
            LOGGER.error("Stale delta received; request delta_at: [%s] is before existing delta_at: [%s]".formatted(
                    request.getInternalData().getDeltaAt(), existingDocument.getDeltaAt()), DataMapHolder.getLogMap());
            return Optional.empty();
        }
        existingDocument.data(mapFilingHistoryData(request.getExternalData(), existingDocument.getData()));

        return Optional.of(mapFilingHistory(request, existingDocument));
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(ExternalData externalData, FilingHistoryData existingData) {
        return dataMapper.map(externalData, existingData);
    }

    @Override
    protected FilingHistoryDocument mapFilingHistory(InternalFilingHistoryApi request,
            FilingHistoryDocument document) {
        final InternalData internalData = request.getInternalData();
        final ExternalData externalData = request.getExternalData();

        return document
                .entityId(internalData.getEntityId())
                .companyNumber(internalData.getCompanyNumber())
                .documentId(internalData.getDocumentId())
                .barcode(externalData.getBarcode())
                .originalDescription(internalData.getOriginalDescription())
                .originalValues(originalValuesMapper.map(internalData.getOriginalValues()))
                .deltaAt(internalData.getDeltaAt())
                .updatedAt(Instant.now())
                .updatedBy(internalData.getUpdatedBy());
    }
}
