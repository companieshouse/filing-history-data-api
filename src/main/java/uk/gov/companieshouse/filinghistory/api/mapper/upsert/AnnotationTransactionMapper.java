package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@Component
public class AnnotationTransactionMapper extends AbstractTransactionMapper {

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

        Optional.ofNullable(existingDocument.getData().getAnnotations())
                .ifPresentOrElse(
                        annotationList -> annotationList.stream()
                                .filter(annotation -> annotation.getEntityId().equals(requestEntityId))
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
                                        () -> annotationList
                                                .add(annotationChildMapper
                                                        .mapChild(new FilingHistoryAnnotation(), request))),
                        // Add new annotation to a new annotations list
                        () -> mapFilingHistoryData(request, existingDocument.getData())
                );
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
