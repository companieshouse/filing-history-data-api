package uk.gov.companieshouse.filinghistory.api.mapper;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.stringToInstant;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;

@Component
public class DescriptionValuesMapper {

    FilingHistoryDescriptionValues map(final FilingHistoryItemDataDescriptionValues descriptionValues) {
        return Optional.ofNullable(descriptionValues)
                .map(values -> new FilingHistoryDescriptionValues()
                        .terminationDate(stringToInstant(values.getTerminationDate()))
                        .officerName(values.getOfficerName()))
                .orElse(null);
    }
}
