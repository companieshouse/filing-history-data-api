package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static uk.gov.companieshouse.filinghistory.api.mapper.get.DateUtils.convertInstantToLocalDateString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataResolutions;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryResolution;

@Component
public class ResolutionsGetResponseMapper {

    private final DescriptionValuesGetResponseMapper descriptionValuesGetResponseMapper;

    public ResolutionsGetResponseMapper(DescriptionValuesGetResponseMapper descriptionValuesGetResponseMapper) {
        this.descriptionValuesGetResponseMapper = descriptionValuesGetResponseMapper;
    }

    public List<FilingHistoryItemDataResolutions> map(List<FilingHistoryResolution> documentResolutions) {
        Optional<List<FilingHistoryResolution>> optionalDocumentResolutions = Optional.ofNullable(documentResolutions);

        return optionalDocumentResolutions.map(inputResolutions -> {
            List<FilingHistoryItemDataResolutions> outputResolutions = new ArrayList<>();
            inputResolutions.forEach(resolution ->
                    outputResolutions.add(
                            new FilingHistoryItemDataResolutions()
                                    .category(resolution.getCategory())
                                    .description(resolution.getDescription())
                                    .type(resolution.getType())
                                    .date(convertInstantToLocalDateString(resolution.getDate()))
                                    .descriptionValues(descriptionValuesGetResponseMapper.map(resolution.getDescriptionValues()))));
            return outputResolutions;
        }).orElse(null);
    }
}
