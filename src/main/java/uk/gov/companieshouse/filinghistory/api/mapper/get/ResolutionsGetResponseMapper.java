package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.instantToString;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataResolutions;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

@Component
public class ResolutionsGetResponseMapper {

    private final DescriptionValuesGetResponseMapper mapper;

    public ResolutionsGetResponseMapper(DescriptionValuesGetResponseMapper mapper) {
        this.mapper = mapper;
    }

    public List<FilingHistoryItemDataResolutions> map(List<FilingHistoryResolution> documentResolutions) {
        return Optional.ofNullable(documentResolutions)
                .map(inputResolutions -> inputResolutions
                        .stream()
                        .map(resolution ->
                                new FilingHistoryItemDataResolutions()
                                        .category(resolution.getCategory())
                                        .description(resolution.getDescription())
                                        .type(resolution.getType())
                                        .date(instantToString(resolution.getDate()))
                                        .descriptionValues(mapper.map(resolution.getDescriptionValues())))
                        .toList())
                .orElse(null);
    }
}
