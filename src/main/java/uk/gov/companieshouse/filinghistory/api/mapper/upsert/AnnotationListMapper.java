package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.stringToInstant;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAnnotations;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;

@Component
public class AnnotationListMapper implements ChildListMapper<FilingHistoryAnnotation> {

    private final DescriptionValuesMapper descriptionValuesMapper;

    public AnnotationListMapper(DescriptionValuesMapper descriptionValuesMapper) {
        this.descriptionValuesMapper = descriptionValuesMapper;
    }

    @Override
    public FilingHistoryAnnotation mapChild(FilingHistoryAnnotation annotation, InternalFilingHistoryApi request) {
        InternalData internalData = request.getInternalData();
        FilingHistoryItemDataAnnotations requestAnnotation = request.getExternalData().getAnnotations().getFirst();

        return annotation
                .entityId(internalData.getEntityId())
                .deltaAt(internalData.getDeltaAt())
                .annotation(requestAnnotation.getAnnotation())
                .category(requestAnnotation.getCategory())
                .date(stringToInstant(requestAnnotation.getDate()))
                .description(requestAnnotation.getDescription())
                .descriptionValues(descriptionValuesMapper.map(requestAnnotation.getDescriptionValues()))
                .type(requestAnnotation.getType());
    }
}
