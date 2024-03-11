package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.stringToInstant;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;

@Component
public class AnnotationListMapper {

    private final DescriptionValuesMapper descriptionValuesMapper;

    public AnnotationListMapper(DescriptionValuesMapper descriptionValuesMapper) {
        this.descriptionValuesMapper = descriptionValuesMapper;
    }

    public List<FilingHistoryAnnotation> addNewAnnotationToList(List<FilingHistoryAnnotation> annotationsList,
                                                                InternalFilingHistoryApi request) {
        annotationsList.add(mapAnnotation(new FilingHistoryAnnotation(), request));
        return annotationsList;
    }

    public void updateExistingAnnotation(FilingHistoryAnnotation annotation, InternalFilingHistoryApi request) {
        mapAnnotation(annotation, request);
    }

    private FilingHistoryAnnotation mapAnnotation(FilingHistoryAnnotation annotation, InternalFilingHistoryApi request) {
        ExternalData externalData = request.getExternalData();
        InternalData internalData = request.getInternalData();

        return annotation
                .entityId(internalData.getEntityId())
                .annotation("annotation") // TODO: Check where this field comes from
                .category(externalData.getCategory().getValue())
                .date(stringToInstant(externalData.getDate()))
                .description(externalData.getDescription())
                .descriptionValues(descriptionValuesMapper.map(externalData.getDescriptionValues()))
                .type(externalData.getType())
                .deltaAt(internalData.getDeltaAt());
    }
}
