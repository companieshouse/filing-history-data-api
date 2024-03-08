package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@Component
public class AnnotationTransactionMapper extends AbstractTransactionMapper {

    private final DataMapper dataMapper;
    private final AnnotationListMapper annotationListMapper;
    private final Supplier<Instant> instantSupplier;

    public AnnotationTransactionMapper(LinksMapper linksMapper, DataMapper dataMapper,
                                       AnnotationListMapper annotationListMapper, Supplier<Instant> instantSupplier) {
        super(linksMapper);
        this.dataMapper = dataMapper;
        this.annotationListMapper = annotationListMapper;
        this.instantSupplier = instantSupplier;
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(ExternalData externalData, FilingHistoryData existingData) {
        return dataMapper.mapNewAnnotation(externalData, existingData);
    }

    @Override
    public FilingHistoryDocument mapFilingHistoryUnlessStale(InternalFilingHistoryApi request,
                                                             FilingHistoryDocument existingDocument) {
        final String requestEntityId = request.getInternalData().getEntityId();

        Optional.ofNullable(existingDocument.getData().getAnnotations())
                .ifPresentOrElse(
                        annotationList ->
                                annotationList.stream()
                                        .filter(annotation -> annotation.getEntityId().equals(requestEntityId))
                                        .findFirst()
                                        .ifPresentOrElse(annotation -> {
                                                    if (isDeltaStale(request.getInternalData().getDeltaAt(),
                                                            annotation.getDeltaAt())) {
                                                        throw new ConflictException(
                                                                "Delta at stale when upserting annotation");
                                                    }
                                                    annotationListMapper.updateExistingAnnotation(annotation);
                                                },
                                                () -> annotationListMapper
                                                        .addNewAnnotationToList(annotationList, request)),
                        () -> existingDocument.getData().annotations(
                                annotationListMapper.addNewAnnotationToList(new ArrayList<>(), request))
                );
        return mapFilingHistory(request, existingDocument);
    }

    @Override
    protected FilingHistoryDocument mapFilingHistory(InternalFilingHistoryApi request, FilingHistoryDocument document) {
        final InternalData internalData = request.getInternalData();

        document.getData().paperFiled(request.getExternalData().getPaperFiled()); // TODO: Check where to set this field for annotation
        return document
                .entityId(internalData.getParentEntityId())
                .companyNumber(internalData.getCompanyNumber())
                .updatedAt(instantSupplier.get())
                .updatedBy(internalData.getUpdatedBy());
    }
}
