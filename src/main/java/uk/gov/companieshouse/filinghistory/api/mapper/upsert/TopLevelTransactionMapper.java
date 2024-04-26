package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import java.time.Instant;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@Component
public class TopLevelTransactionMapper extends AbstractTransactionMapper {

    private final DataMapper dataMapper;
    private final Supplier<Instant> instantSupplier;
    private final OriginalValuesMapper originalValuesMapper;

    public TopLevelTransactionMapper(DataMapper dataMapper, Supplier<Instant> instantSupplier,
            OriginalValuesMapper originalValuesMapper, LinksMapper linksMapper) {
        super(linksMapper);
        this.dataMapper = dataMapper;
        this.instantSupplier = instantSupplier;
        this.originalValuesMapper = originalValuesMapper;
    }

    @Override
    public FilingHistoryDocument mapFilingHistoryToExistingDocumentUnlessStale(InternalFilingHistoryApi request,
                                                                               FilingHistoryDocument existingDocument,
            Instant instant) {
        if (isDeltaStale(request.getInternalData().getDeltaAt(), existingDocument.getDeltaAt())) {
            LOGGER.error("Stale delta received; request delta_at: [%s] is not after existing delta_at: [%s]".formatted(
                    request.getInternalData().getDeltaAt(), existingDocument.getDeltaAt()), DataMapHolder.getLogMap());

            throw new ConflictException("Stale delta for upsert");
        }
        existingDocument.data(mapFilingHistoryData(request, existingDocument.getData()));

        return mapTopLevelFields(request, existingDocument, instant);
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(InternalFilingHistoryApi request, FilingHistoryData data) {
        return dataMapper.map(request.getExternalData(), data);
    }

    @Override
    protected FilingHistoryDocument mapTopLevelFields(InternalFilingHistoryApi request,
                                                      FilingHistoryDocument document, Instant instant) {
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
                .updated(new FilingHistoryDeltaTimestamp(instant, request.getInternalData().getUpdatedBy()));
    }
}
