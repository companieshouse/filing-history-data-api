package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;
import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.isDeltaStale;

import java.time.Instant;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAssociatedFiling;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class TopLevelTransactionMapper extends AbstractTransactionMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final DataMapper dataMapper;
    private final ChildListMapper<FilingHistoryAssociatedFiling> childListMapper;
    private final OriginalValuesMapper originalValuesMapper;

    public TopLevelTransactionMapper(DataMapper dataMapper,
            OriginalValuesMapper originalValuesMapper, LinksMapper linksMapper,
            ChildListMapper<FilingHistoryAssociatedFiling> childListMapper) {
        super(linksMapper);
        this.dataMapper = dataMapper;
        this.originalValuesMapper = originalValuesMapper;
        this.childListMapper = childListMapper;
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(InternalFilingHistoryApi request, FilingHistoryData data) {
        ExternalData externalData = request.getExternalData();
        final FilingHistoryData mappedData = dataMapper.map(externalData, data);

        if (externalData.getAssociatedFilings() != null && !externalData.getAssociatedFilings().isEmpty()) {
            childListMapper.mapChildList(request, mappedData.getAssociatedFilings(), mappedData::associatedFilings);

            data.getAssociatedFilings().stream()
                    .filter(af -> request.getInternalData().getEntityId().equals(af.getEntityId()))
                    .findFirst()
                    .ifPresent(af -> af.originalDescription(
                            request.getExternalData().getAssociatedFilings().getFirst().getOriginalDescription()));
        }

        return mappedData;
    }

    @Override
    protected FilingHistoryDocument mapTopLevelFields(InternalFilingHistoryApi request, FilingHistoryDocument document,
            Instant instant) {
        if (isDeltaStale(request.getInternalData().getDeltaAt(), document.getDeltaAt())) {
            LOGGER.error("Stale delta received; request delta_at: [%s] is not after existing delta_at: [%s]".formatted(
                    request.getInternalData().getDeltaAt(), document.getDeltaAt()), DataMapHolder.getLogMap());
            throw new ConflictException("Stale delta for upsert");
        }

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
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(instant)
                        .by(internalData.getUpdatedBy()));
    }
}
