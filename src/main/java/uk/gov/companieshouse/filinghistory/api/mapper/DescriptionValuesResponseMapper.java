package uk.gov.companieshouse.filinghistory.api.mapper;

import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;

@Component
public class DescriptionValuesResponseMapper {

    public FilingHistoryItemDataDescriptionValues map(FilingHistoryDescriptionValues descriptionValues) {
        return new FilingHistoryItemDataDescriptionValues()
                .officerName(descriptionValues.getOfficerName())
                .terminationDate(LocalDate.ofInstant(descriptionValues.getTerminationDate(), ZoneOffset.UTC).toString());
    }
}
