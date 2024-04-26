package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

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
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@Component
public class AnnotationTransactionMapper extends AbstractTransactionMapper {

    private final DataMapper dataMapper;
    private final ChildMapper<FilingHistoryAnnotation> annotationChildMapper;
    private final Supplier<Instant> instantSupplier;

    public AnnotationTransactionMapper(LinksMapper linksMapper,
            DataMapper dataMapper,
            ChildMapper<FilingHistoryAnnotation> annotationChildMapper,
            Supplier<Instant> instantSupplier) {
        super(linksMapper);
        this.dataMapper = dataMapper;
        this.annotationChildMapper = annotationChildMapper;
        this.instantSupplier = instantSupplier;
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(InternalFilingHistoryApi request, FilingHistoryData data) {
        if (StringUtils.isBlank(request.getInternalData().getParentEntityId())) {
            data = dataMapper.map(request.getExternalData(), data);
        }
        return data.annotations(List.of(annotationChildMapper.mapChild(new FilingHistoryAnnotation(), request)));
    }

    @Override
    public FilingHistoryDocument mapFilingHistoryToExistingDocumentUnlessStale(InternalFilingHistoryApi request,
            FilingHistoryDocument existingDocument, Instant instant) {
        final String requestEntityId = request.getInternalData().getEntityId();

        Optional.ofNullable(existingDocument.getData().getAnnotations())
                .ifPresentOrElse(
                        annotationList -> annotationList.stream()
                                .filter(annotation -> requestEntityId.equals(annotation.getEntityId()))
                                .findFirst()
                                .ifPresentOrElse(annotation -> {
                                            if (isDeltaStale(request.getInternalData().getDeltaAt(),
                                                    annotation.getDeltaAt())) {
                                                LOGGER.error(STALE_DELTA_ERROR_MESSAGE.formatted(
                                                                request.getInternalData().getDeltaAt(),
                                                                annotation.getDeltaAt()),
                                                        DataMapHolder.getLogMap());
                                                throw new ConflictException("Stale delta when updating annotation");
                                            }
                                            // Update already existing annotation from list
                                            annotationChildMapper.mapChild(annotation, request);
                                        },
                                        // Add new annotation to existing annotations list
                                        () -> {
                                            if (annotationList.stream()
                                                    .anyMatch(annotation ->
                                                            StringUtils.isBlank(annotation.getEntityId()))) {
                                                LOGGER.info(
                                                        MISSING_ENTITY_ID_ERROR_MSG.formatted(requestEntityId),
                                                        DataMapHolder.getLogMap()
                                                );
                                            }
                                            annotationList
                                                    .add(annotationChildMapper
                                                            .mapChild(new FilingHistoryAnnotation(), request));
                                        }),
                        // Add new annotation to a new annotations list
                        () -> mapFilingHistoryData(request, existingDocument.getData())
                );
        return mapTopLevelFields(request, existingDocument, instant);
    }

    @Override
    protected FilingHistoryDocument mapTopLevelFields(InternalFilingHistoryApi request, FilingHistoryDocument document,
            Instant instant) {
        document.getData().paperFiled(request.getExternalData().getPaperFiled());

        final InternalData internalData = request.getInternalData();

        if (StringUtils.isBlank(internalData.getParentEntityId())) {
            document
                    .entityId(internalData.getEntityId())
                    .barcode(request.getExternalData().getBarcode())
                    .documentId(internalData.getDocumentId())
                    .deltaAt(internalData.getDeltaAt())
                    .matchedDefault(internalData.getMatchedDefault())
                    .originalDescription(internalData.getOriginalDescription());
        } else {
            document
                    .entityId(internalData.getParentEntityId());
        }
        return document
                .companyNumber(internalData.getCompanyNumber())
                .updated(new FilingHistoryDeltaTimestamp(instant, request.getInternalData().getUpdatedBy()));
    }
}
