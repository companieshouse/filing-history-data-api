package uk.gov.companieshouse.filinghistory.api.mapper.get;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;

@Component
public class DescriptionValuesCleanser {

    FilingHistoryItemDataDescriptionValues replaceBackslashesWithWhiteSpace(CategoryEnum category,
            FilingHistoryItemDataDescriptionValues descriptionValues) {
        return null;
    }
}
