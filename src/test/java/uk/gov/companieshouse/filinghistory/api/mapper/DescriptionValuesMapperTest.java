package uk.gov.companieshouse.filinghistory.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;

class DescriptionValuesMapperTest {

    private static final String OFFICER_NAME = "John Tester";
    private static final String TERMINATION_DATE = "2020-06-10T00:00:00.00Z";
    private static final Instant TERMINATION_DATE_AS_INSTANT = Instant.parse(TERMINATION_DATE);

    private final DescriptionValuesMapper mapper = new DescriptionValuesMapper();

    @Test
    void shouldMapDescriptionValues() {
        // given
        FilingHistoryItemDataDescriptionValues descriptionValues = new FilingHistoryItemDataDescriptionValues()
                .officerName(OFFICER_NAME)
                .terminationDate(TERMINATION_DATE);

        FilingHistoryDescriptionValues expected = new FilingHistoryDescriptionValues()
                .officerName(OFFICER_NAME)
                .terminationDate(TERMINATION_DATE_AS_INSTANT);

        // when
        FilingHistoryDescriptionValues actual = mapper.map(descriptionValues);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnNullWhenDescriptionValuesNull() {
        // given

        // when
        FilingHistoryDescriptionValues actual = mapper.map(null);

        // then
        assertNull(actual);
    }

}