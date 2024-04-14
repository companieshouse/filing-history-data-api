package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.stringToInstant;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.api.filinghistory.Resolution;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

@Component
public class ResolutionChildMapper implements ChildMapper<FilingHistoryResolution> {

    private final DescriptionValuesMapper descriptionValuesMapper;

    public ResolutionChildMapper(DescriptionValuesMapper descriptionValuesMapper) {
        this.descriptionValuesMapper = descriptionValuesMapper;
    }

    @Override
    public FilingHistoryResolution mapChild(FilingHistoryResolution resolution, InternalFilingHistoryApi request) {
        InternalData internalData = request.getInternalData();
        Resolution requestResolution = request.getExternalData().getResolutions().getFirst();

        return resolution
                .category(requestResolution.getCategory().getValue())
                .description(requestResolution.getDescription())
                .type(requestResolution.getType())
                .subcategory(requestResolution.getSubcategory())
                .date(stringToInstant(requestResolution.getDate()))
                .entityId(internalData.getEntityId())
                .descriptionValues(descriptionValuesMapper.map(requestResolution.getDescriptionValues()))
                .deltaAt(internalData.getDeltaAt());
    }
}