package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAssociatedFiling;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@Component
public class TopLevelTransactionMapper extends AbstractTransactionMapper {

    private final DataMapper dataMapper;
    private final AssociatedFilingChildMapper associatedFilingChildMapper;
    private final Supplier<Instant> instantSupplier;
    private final OriginalValuesMapper originalValuesMapper;

    public TopLevelTransactionMapper(DataMapper dataMapper, Supplier<Instant> instantSupplier,
                                     OriginalValuesMapper originalValuesMapper, LinksMapper linksMapper,
                                     AssociatedFilingChildMapper associatedFilingChildMapper) {
        super(linksMapper);
        this.dataMapper = dataMapper;
        this.instantSupplier = instantSupplier;
        this.originalValuesMapper = originalValuesMapper;
        this.associatedFilingChildMapper = associatedFilingChildMapper;
    }

    @Override
    public FilingHistoryDocument mapFilingHistoryToExistingDocumentUnlessStale(InternalFilingHistoryApi request,
                                                                               FilingHistoryDocument existingDocument) {
        if (isDeltaStale(request.getInternalData().getDeltaAt(), existingDocument.getDeltaAt())) {
            LOGGER.error("Stale delta received; request delta_at: [%s] is not after existing delta_at: [%s]".formatted(
                    request.getInternalData().getDeltaAt(), existingDocument.getDeltaAt()), DataMapHolder.getLogMap());

            throw new ConflictException("Stale delta for upsert");
        }
        existingDocument.data(mapFilingHistoryData(request, existingDocument.getData()));

        return mapTopLevelFields(request, existingDocument);
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(InternalFilingHistoryApi request, FilingHistoryData data) {
        ExternalData externalData = request.getExternalData();
        final String requestEntityId = request.getInternalData().getEntityId();
        final FilingHistoryData mappedData = dataMapper.map(externalData, data);

        if (externalData.getAssociatedFilings() != null && !externalData.getAssociatedFilings().isEmpty()) {

            Optional.ofNullable(mappedData.getAssociatedFilings())
                    .ifPresentOrElse(
                            associatedFilingList -> associatedFilingList.stream()
                                    .filter(associatedFiling -> requestEntityId.equals(associatedFiling.getEntityId()))
                                    .findFirst()
                                    .ifPresentOrElse(associatedFiling -> {
                                                if (isDeltaStale(request.getInternalData().getDeltaAt(),
                                                        associatedFiling.getDeltaAt())) {
                                                    LOGGER.error(STALE_DELTA_ERROR_MESSAGE.formatted(
                                                                    request.getInternalData().getDeltaAt(),
                                                                    associatedFiling.getDeltaAt()),
                                                            DataMapHolder.getLogMap());
                                                    throw new ConflictException(
                                                            "Stale delta when updating associated filing");
                                                }
                                                // Update already existing associated filing from existing list
                                                associatedFilingChildMapper.mapChild(associatedFiling, request);
                                            },
                                            // Add new associated filing to existing list
                                            () -> {
                                                if (associatedFilingList.stream()
                                                        .anyMatch(associatedFiling ->
                                                                StringUtils.isBlank(associatedFiling.getEntityId()))) {
                                                    LOGGER.info(
                                                            MISSING_ENTITY_ID_ERROR_MSG.formatted(requestEntityId),
                                                            DataMapHolder.getLogMap()
                                                    );
                                                }
                                                associatedFilingList
                                                        .add(associatedFilingChildMapper
                                                                .mapChild(new FilingHistoryAssociatedFiling(), request));
                                            }),
                            // Add new associated filing to a new associated filing list
                            () -> mappedData.associatedFilings(List.of(
                                    associatedFilingChildMapper.mapChild(new FilingHistoryAssociatedFiling(), request)))
                    );
        }
        return mappedData;
    }

    @Override
    protected FilingHistoryDocument mapTopLevelFields(InternalFilingHistoryApi request,
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
                .updatedAt(instantSupplier.get())
                .updatedBy(internalData.getUpdatedBy());
    }
}
