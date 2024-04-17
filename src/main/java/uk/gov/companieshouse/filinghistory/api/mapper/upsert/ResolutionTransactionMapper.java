package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.stringToInstant;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
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
            FilingHistoryDocument existingDocument) {

        final String requestEntityId = request.getInternalData().getEntityId();

        Optional.ofNullable(existingDocument.getData().getResolutions())
                .ifPresentOrElse(
                        resolutionList -> resolutionList.stream()
                                .filter(resolution -> resolution.getEntityId().equals(requestEntityId))
                                .findFirst()
                                .ifPresentOrElse(resolution -> {
                                            if (isDeltaStale(request.getInternalData().getDeltaAt(),
                                                    resolution.getDeltaAt())) {
                                                throw new ConflictException(
                                                        "Delta at stale when updating resolution");
                                            }

                                            // Update already existing resolution from list
                                            resolutionChildMapper.mapChild(resolution, request);
                                        },
                                        // Add new resolution to existing resolutions list
                                        () -> resolutionList
                                                .add(resolutionChildMapper
                                                        .mapChild(new FilingHistoryResolution(), request))),
                        // Add new resolution to a new resolutions list
                        () -> {
                            LOGGER.error("Unexpected resolution data structure, adding new resolutions array",
                                    DataMapHolder.getLogMap());
                            mapFilingHistoryData(request, existingDocument.getData());
                        }
                );
        return mapTopLevelFields(request, existingDocument);
    }

    @Override
    protected FilingHistoryDocument mapTopLevelFields(InternalFilingHistoryApi request,
            FilingHistoryDocument document) {
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
