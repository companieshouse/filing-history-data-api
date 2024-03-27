package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static java.util.regex.Matcher.quoteReplacement;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;

@Component
public class DescriptionValuesCleanser {

    private final ObjectMapper objectMapper;

    public DescriptionValuesCleanser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    DescriptionValues replaceBackslashesWithWhitespace(CategoryEnum category, DescriptionValues descriptionValues) {
        if (CategoryEnum.ADDRESS.equals(category)) {
            Map<String, Object> values = objectMapper.convertValue(descriptionValues, new TypeReference<>() {
            });
            Map<String, Object> cleansedValues = new HashMap<>();
            values.forEach((key, value) -> {
                if (value instanceof String s) {
                    value = s.replaceAll(quoteReplacement("\\"), " ");
                }
                cleansedValues.put(key, value);
            });
            return objectMapper.convertValue(cleansedValues, DescriptionValues.class);
        }
        return descriptionValues;
    }
}
