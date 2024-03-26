package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;

class DescriptionValuesCleanserTest {

    private final DescriptionValuesCleanser cleanser = new DescriptionValuesCleanser(new ObjectMapper());

    @Test
    void shouldReplaceBackslashesWithWhitespace() {
        // given
        DescriptionValues values = new DescriptionValues()
                .newAddress("C\\O address")
                .date("2010-05-01")
                .caseEndDate("17/10/98");

        DescriptionValues expected = new DescriptionValues()
                .newAddress("C O address")
                .date("2010-05-01")
                .caseEndDate("17/10/98");

        // when
        DescriptionValues actual = cleanser.replaceBackslashesWithWhitespace(CategoryEnum.ADDRESS, values);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldNotReplaceIfCategoryNotAddress() {
        // given
        DescriptionValues values = new DescriptionValues()
                .description("a\\description");

        DescriptionValues expected = new DescriptionValues()
                .description("a\\description");

        // when
        DescriptionValues actual = cleanser.replaceBackslashesWithWhitespace(CategoryEnum.INSOLVENCY, values);

        // then
        assertEquals(expected, actual);
    }
}