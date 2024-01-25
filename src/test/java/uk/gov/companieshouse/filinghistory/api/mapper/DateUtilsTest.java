package uk.gov.companieshouse.filinghistory.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class DateUtilsTest {

    @Test
    void shouldStringToInstant() {
        // given
        String date = "20200610000000";

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