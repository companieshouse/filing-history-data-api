package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class AnnotationTransactionMapper extends AbstractTransactionMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String MISSING_ENTITY_ID_ERROR_MSG =
            "Child found in MongoDB with no _entity_id; Possible duplicate being persisted with _entity_id: [%s]";

    private final ChildMapper<FilingHistoryAnnotation> annotationChildMapper;
    private final Supplier<Instant> instantSupplier;

    public AnnotationTransactionMapper(LinksMapper linksMapper,
                                       ChildMapper<FilingHistoryAnnotation> annotationChildMapper,
                                       Supplier<Instant> instantSupplier) {
        super(linksMapper);
        this.annotationChildMapper = annotationChildMapper;
        this.instantSupplier = instantSupplier;
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(InternalFilingHistoryApi request, FilingHistoryData data) {
        return data.annotations(List.of(annotationChildMapper.mapChild(new FilingHistoryAnnotation(), request)));
    }

    @Override
    public FilingHistoryDocument mapFilingHistoryToExistingDocumentUnlessStale(InternalFilingHistoryApi request,
                                                                               FilingHistoryDocument existingDocument) {
        final String requestEntityId = request.getInternalData().getEntityId();
        List<FilingHistoryAnnotation> existingAnnotationsList = existingDocument.getData().getAnnotations();

        if (existingAnnotationsList == null) {
            // Add new annotation to a new annotations list
            mapFilingHistoryData(request, existingDocument.getData());
        } else {
            // Check for legacy data without entity id
            for (FilingHistoryAnnotation annotation : existingAnnotationsList) {
                if (annotation.getEntityId() == null) {
                    LOGGER.error(MISSING_ENTITY_ID_ERROR_MSG.formatted(requestEntityId), DataMapHolder.getLogMap());
                    break;
                }
            }

            existingAnnotationsList.stream()
                    .filter(annotation -> requestEntityId.equals(annotation.getEntityId()))
                    .findFirst()
                    .ifPresentOrElse(annotation -> {
                                if (isDeltaStale(request.getInternalData().getDeltaAt(),
                                        annotation.getDeltaAt())) {
                                    throw new ConflictException(
                                            "Delta at stale when updating annotation");
                                }
                                // Update already existing annotation from list
                                annotationChildMapper.mapChild(annotation, request);
                            },
                            // Add new annotation to existing annotations list
                            () -> existingAnnotationsList
                                    .add(annotationChildMapper
                                            .mapChild(new FilingHistoryAnnotation(), request)));
        }
        return mapTopLevelFields(request, existingDocument);
    }

    @Override
    protected FilingHistoryDocument mapTopLevelFields(InternalFilingHistoryApi request, FilingHistoryDocument document) {
        final InternalData internalData = request.getInternalData();

        document.getData().paperFiled(request.getExternalData().getPaperFiled());
        return document
                .entityId(internalData.getParentEntityId())
                .companyNumber(internalData.getCompanyNumber())
                .updatedAt(instantSupplier.get())
                .updatedBy(internalData.getUpdatedBy());
    }
}
