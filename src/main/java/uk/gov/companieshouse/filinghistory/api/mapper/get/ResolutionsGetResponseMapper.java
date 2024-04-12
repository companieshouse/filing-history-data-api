package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.instantToString;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.Resolution;
import uk.gov.companieshouse.api.filinghistory.Resolution.CategoryEnum;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

@Component
public class ResolutionsGetResponseMapper {

    private final DescriptionValuesGetResponseMapper mapper;

    public ResolutionsGetResponseMapper(DescriptionValuesGetResponseMapper mapper) {
        this.mapper = mapper;
    }

    public List<Resolution> map(List<FilingHistoryResolution> documentResolutions) {
        return Optional.ofNullable(documentResolutions)
                .map(inputResolutions -> inputResolutions
                        .stream()
                        .map(resolution ->
                                new Resolution()
                                        .category(CategoryEnum.fromValue(resolution.getCategory()))
                                        .subcategory(resolution.getSubcategory())
                                        .description(resolution.getDescription())
                                        .type(resolution.getType())
                                        .date(instantToString(resolution.getDate()))
                                        .descriptionValues(mapper.map(resolution.getDescriptionValues()))
                                        .originalDescription(resolution.getOriginalDescription())
                                        .deltaAt(resolution.getDeltaAt()))
                        .toList())
                .orElse(null);
    }
}
