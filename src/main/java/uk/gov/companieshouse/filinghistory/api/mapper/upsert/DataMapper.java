package uk.gov.companieshouse.filinghistory.api.mapper.upsert;


import static uk.gov.companieshouse.filinghistory.api.mapper.upsert.DateUtils.stringToInstant;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;

@Component
public class DataMapper {

    private final DescriptionValuesMapper descriptionValuesMapper;

    public DataMapper(DescriptionValuesMapper descriptionValuesMapper) {
        this.descriptionValuesMapper = descriptionValuesMapper;
    }

    FilingHistoryData map(final ExternalData data, final FilingHistoryData documentData) {
        return documentData
                .type(data.getType())
                .date(stringToInstant(data.getDate()))
                .category(data.getCategory().toString())
                .subcategory(data.getSubcategory().toString())
                .description(data.getDescription())
                .descriptionValues(descriptionValuesMapper.map(data.getDescriptionValues()))
                .actionDate(stringToInstant(data.getActionDate()))
                .paperFiled(data.getPaperFiled());
    }
}
