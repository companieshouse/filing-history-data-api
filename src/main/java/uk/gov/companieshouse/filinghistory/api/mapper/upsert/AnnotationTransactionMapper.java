package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@Component
public class AnnotationTransactionMapper extends AbstractTransactionMapper {

    private final DataMapper dataMapper;
    private final AnnotationListMapper annotationListMapper;
    private final Supplier<Instant> instantSupplier;
    private final OriginalValuesMapper originalValuesMapper;

    public AnnotationTransactionMapper(LinksMapper linksMapper, DataMapper dataMapper, AnnotationListMapper annotationListMapper,
                                       Supplier<Instant> instantSupplier, OriginalValuesMapper originalValuesMapper) {
        super(linksMapper);
        this.dataMapper = dataMapper;
        this.annotationListMapper = annotationListMapper;
        this.instantSupplier = instantSupplier;
        this.originalValuesMapper = originalValuesMapper;
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(ExternalData externalData, FilingHistoryData existingData) {
        return dataMapper.mapNewAnnotation(externalData, existingData);
    }

    @Override
    public FilingHistoryDocument mapFilingHistoryUnlessStale(InternalFilingHistoryApi request, FilingHistoryDocument existingDocument) {
        return mapFilingHistory(request, existingDocument);
    }

    @Override
    protected FilingHistoryDocument mapFilingHistory(InternalFilingHistoryApi request, FilingHistoryDocument document) {
        final InternalData internalData = request.getInternalData();
        final ExternalData externalData = request.getExternalData();

        // TODO: May need to find a more elegant solution to mapping child entity ids
        List<FilingHistoryAnnotation> annotationsList = document.getData().getAnnotations();
        if (!annotationsList.isEmpty() && StringUtils.isBlank(annotationsList.getFirst().getEntityId())) {
            annotationsList
                    .getFirst()
                    .entityId(request.getInternalData().getEntityId());
        }

        return document
                .entityId(request.getInternalData().getEntityId())
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
