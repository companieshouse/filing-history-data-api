package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;
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
    private final Supplier<Instant> instantSupplier;
    private final OriginalValuesMapper originalValuesMapper;

    public AnnotationTransactionMapper(LinksMapper linksMapper, DataMapper dataMapper,
                                       Supplier<Instant> instantSupplier, OriginalValuesMapper originalValuesMapper) {
        super(linksMapper);
        this.dataMapper = dataMapper;
        this.instantSupplier = instantSupplier;
        this.originalValuesMapper = originalValuesMapper;
    }

    @Override
    protected FilingHistoryData mapFilingHistoryData(ExternalData externalData, FilingHistoryData existingData) {
        return dataMapper.mapNewAnnotation(externalData, existingData);
    }

    @Override
    public FilingHistoryDocument mapFilingHistoryUnlessStale(InternalFilingHistoryApi request, FilingHistoryDocument existingDocument) {
        List<FilingHistoryAnnotation> annotationList = existingDocument.getData().getAnnotations();
        if (annotationList == null || annotationList.isEmpty()) {
            existingDocument.data(dataMapper.mapNewAnnotation(request.getExternalData(), existingDocument.getData()));
            return mapFilingHistory(request, existingDocument);
        }

        for (final FilingHistoryAnnotation annotation : annotationList) {
            if (annotation.getEntityId().equals(request.getInternalData().getEntityId())) {
                existingDocument.data(dataMapper.mapExistingAnnotation(request.getExternalData(), existingDocument.getData()));
                break;
            } else {
                existingDocument.data(dataMapper.mapNewAnnotation(request.getExternalData(), existingDocument.getData()));
            }
        }
        return mapFilingHistory(request, existingDocument);

        // TODO: This is a work in progress and needs cleaning up
    }

    @Override
    protected FilingHistoryDocument mapFilingHistory(InternalFilingHistoryApi request, FilingHistoryDocument document) {
        final InternalData internalData = request.getInternalData();
        final ExternalData externalData = request.getExternalData();

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
