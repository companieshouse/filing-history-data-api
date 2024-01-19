package uk.gov.companieshouse.filinghistory.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryOriginalValues;

class OriginalValuesMapperTest {

    private static final String OFFICER_NAME = "John Tester";
    private static final String RESIGNATION_DATE = "06/08/2011";
    private final OriginalValuesMapper mapper = new OriginalValuesMapper();

    @Test
    void shouldMapOriginalValues() {
        // given
        InternalDataOriginalValues originalValues = new InternalDataOriginalValues()
                .officerName(OFFICER_NAME)
                .resignationDate(RESIGNATION_DATE);

        FilingHistoryOriginalValues expected = new FilingHistoryOriginalValues()
                .officerName(OFFICER_NAME)
                .resignationDate(RESIGNATION_DATE);

        // when
        FilingHistoryOriginalValues actual = mapper.map(originalValues);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnNullWhenOriginalValuesNull() {
        // given

        // when
        FilingHistoryOriginalValues actual = mapper.map(null);

        // then
        assertNull(actual);
    }
}