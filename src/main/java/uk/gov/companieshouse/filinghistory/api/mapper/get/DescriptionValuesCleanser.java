package uk.gov.companieshouse.filinghistory.api.mapper.get;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;

@Component
public class DescriptionValuesCleanser {

    DescriptionValues replaceBackslashesWithWhiteSpace(CategoryEnum category,
            DescriptionValues descriptionValues) {
        return null;
    }
}
