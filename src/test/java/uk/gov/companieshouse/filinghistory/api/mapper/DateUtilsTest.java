package uk.gov.companieshouse.filinghistory.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DateUtilsTest {

    @Test
    void shouldConvertLocalDateToInstant() {
        // given
        LocalDate date = LocalDate.of(2020, 6, 10);

        // when
        Instant actual = DateUtils.localDateToInstant(date);

        // then
        assertEquals(Instant.ofEpochSecond(1591747200), actual);
    }

    @Test
    void convertLocalDateToInstantShouldReturnNullWhenLocalDateNull() {
        // given
        // when
        Instant actual = DateUtils.localDateToInstant(null);

        // then
        assertNull(actual);
    }
}