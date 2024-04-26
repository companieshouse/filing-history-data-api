package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.stringToInstant;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

@Component
public class ResolutionTransactionMapper extends AbstractTransactionMapper {

    private final DataMapper dataMapper;
    private final ChildMapper<FilingHistoryResolution> resolutionChildMapper;
    private final Supplier<Instant> instantSupplier;

    public ResolutionTransactionMapper(LinksMapper linksMapper, DataMapper dataMapper,
            ChildMapper<FilingHistoryResolution> resolutionChildMapper, Supplier<Instant> instantSupplier) {
        super(linksMapper);
        this.dataMapper = dataMapper;
        this.resolutionChildMapper = resolutionChildMapper;
        this.instantSupplier = instantSupplier;
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(InternalFilingHistoryApi request, FilingHistoryData data) {
        return dataMapper.map(request.getExternalData(), data)
                .resolutions(List.of(resolutionChildMapper.mapChild(new FilingHistoryResolution(), request)));
    }

    @Override
    public FilingHistoryDocument mapFilingHistoryToExistingDocumentUnlessStale(InternalFilingHistoryApi request,
            FilingHistoryDocument existingDocument,
            Instant instant) {

        final String requestEntityId = request.getInternalData().getEntityId();

        Optional.ofNullable(existingDocument.getData().getResolutions())
                .ifPresentOrElse(
                        resolutionList -> resolutionList.stream()
                                .filter(resolution -> requestEntityId.equals(resolution.getEntityId()))
                                .findFirst()
                                .ifPresentOrElse(resolution -> {
                                            if (isDeltaStale(request.getInternalData().getDeltaAt(),
                                                    resolution.getDeltaAt())) {
                                                LOGGER.error(STALE_DELTA_ERROR_MESSAGE.formatted(
                                                                request.getInternalData().getDeltaAt(),
                                                                resolution.getDeltaAt()),
                                                        DataMapHolder.getLogMap());
                                                throw new ConflictException("Stale delta when updating resolution");
                                            }

                                            // Update already existing resolution from list
                                            resolutionChildMapper.mapChild(resolution, request);
                                        },
                                        // Add new resolution to existing resolutions list
                                        () -> {
                                            if (resolutionList.stream()
                                                    .anyMatch(resolution -> StringUtils.isBlank(
                                                            resolution.getEntityId()))) {
                                                LOGGER.info(
                                                        MISSING_ENTITY_ID_ERROR_MSG.formatted(requestEntityId),
                                                        DataMapHolder.getLogMap()
                                                );
                                            }
                                            resolutionList
                                                    .add(resolutionChildMapper
                                                            .mapChild(new FilingHistoryResolution(), request));
                                        }),
                        // Add new resolution to a new resolutions list
                        () -> {
                            LOGGER.error("Unexpected resolution data structure, adding new resolutions array",
                                    DataMapHolder.getLogMap());
                            mapFilingHistoryData(request, existingDocument.getData());
                        }
                );
        return mapTopLevelFields(request, existingDocument, instant);
    }

    @Override
    protected FilingHistoryDocument mapTopLevelFields(InternalFilingHistoryApi request,
            FilingHistoryDocument document, Instant instant) {
        final InternalData internalData = request.getInternalData();

        document.getData().paperFiled(request.getExternalData().getPaperFiled());
        document.getData().date(stringToInstant(request.getExternalData().getDate()));
        return document
                .companyNumber(internalData.getCompanyNumber())
                .entityId(internalData.getEntityId())
                .deltaAt(internalData.getDeltaAt())
                .updatedAt(instantSupplier.get())
                .updatedBy(internalData.getUpdatedBy())
                .barcode(request.getExternalData().getBarcode())
                .originalDescription(internalData.getOriginalDescription());
    }
}
