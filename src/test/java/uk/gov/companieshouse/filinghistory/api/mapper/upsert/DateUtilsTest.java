package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.filinghistory.api.mapper.DateUtils;

class DateUtilsTest {

    @Test
    void shouldConvertStringToInstant() {
        // given
        String date = "2020-06-10T00:00:00.00Z";

        // when
        Instant actual = DateUtils.stringToInstant(date);

        // then
        assertEquals(Instant.ofEpochSecond(1591747200), actual);
    }

    @Test
    void convertStringToInstantShouldReturnNullWhenStringNull() {
        // given
        // when
        Instant actual = DateUtils.stringToInstant(null);

        // then
        assertNull(actual);
    }

    @Test
    void shouldConvertInstantToString() {
        // given
        final String expected = "2020-06-10";
        final Instant input = Instant.parse("2020-06-10T00:00:00.00Z");

        // when
        final String actual = DateUtils.convertInstantToLocalDateString(input);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void convertInstantToStringShouldReturnNullWhenInstantNull() {
        // given

        // when
        final String actual = DateUtils.convertInstantToLocalDateString(null);

        // then
        assertNull(actual);
    }
}