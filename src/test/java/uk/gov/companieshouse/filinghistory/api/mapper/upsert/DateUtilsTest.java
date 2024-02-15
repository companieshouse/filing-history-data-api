package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.filinghistory.api.mapper.DateUtils;

class DateUtilsTest {

    @Test
    void shouldStringToInstant() {
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
}