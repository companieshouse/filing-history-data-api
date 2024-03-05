package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;
import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.stringToInstant;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.filinghistory.api.exception.BadRequestException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class DataMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final DescriptionValuesMapper descriptionValuesMapper;

    public DataMapper(DescriptionValuesMapper descriptionValuesMapper) {
        this.descriptionValuesMapper = descriptionValuesMapper;
    }

    @SuppressWarnings("unchecked")
    FilingHistoryData map(final ExternalData data, final FilingHistoryData documentData) {
        switch (data.getSubcategory()) {
            case null -> {
            }
            case String subcategory -> documentData.subcategory(subcategory);
            case List<?> subcategory -> documentData.subcategory((List<String>) subcategory);
            default -> {
                String message = "Invalid subcategory type: [%s]".formatted(data.getSubcategory().getClass());
                LOGGER.error(message, DataMapHolder.getLogMap());
                throw new BadRequestException(message);
            }
        }

        return documentData
                .type(data.getType())
                .date(stringToInstant(data.getDate()))
                .category(data.getCategory().toString())
                .description(data.getDescription())
                .descriptionValues(descriptionValuesMapper.map(data.getDescriptionValues()))
                .actionDate(stringToInstant(data.getActionDate()))
                .paperFiled(data.getPaperFiled());
    }
}
