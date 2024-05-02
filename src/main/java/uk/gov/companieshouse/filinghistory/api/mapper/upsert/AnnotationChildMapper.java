package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.stringToInstant;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.Annotation;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAnnotation;

@Component
public class AnnotationChildMapper implements ChildMapper<FilingHistoryAnnotation> {

    private final DescriptionValuesMapper descriptionValuesMapper;

    public AnnotationChildMapper(DescriptionValuesMapper descriptionValuesMapper) {
        this.descriptionValuesMapper = descriptionValuesMapper;
    }

    @Override
    public FilingHistoryAnnotation mapChild(InternalFilingHistoryApi request,
            FilingHistoryAnnotation existingAnnotation) {
        InternalData internalData = request.getInternalData();
        Annotation requestAnnotation = request.getExternalData().getAnnotations().getFirst();

        return existingAnnotation
                .entityId(internalData.getEntityId())
                .deltaAt(internalData.getDeltaAt())
                .annotation(requestAnnotation.getAnnotation())
                .category(requestAnnotation.getCategory())
                .date(stringToInstant(requestAnnotation.getDate()))
                .description(requestAnnotation.getDescription())
                .descriptionValues(descriptionValuesMapper.map(requestAnnotation.getDescriptionValues()))
                .type(requestAnnotation.getType());
    }

    @Override
    public FilingHistoryAnnotation newInstance() {
        return new FilingHistoryAnnotation();
    }
}
