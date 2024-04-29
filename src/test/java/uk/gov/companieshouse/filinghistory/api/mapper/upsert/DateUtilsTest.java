package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.filinghistory.api.mapper.DateUtils;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;

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
        final String actual = DateUtils.instantToString(input);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void convertInstantToStringShouldReturnNullWhenInstantNull() {
        // given

        // when
        final String actual = DateUtils.instantToString(null);

        // then
        assertNull(actual);
    }

    @Test
    void shouldReturnNewDeltaTimeStampObject() {
        // given
        FilingHistoryDeltaTimestamp expected = new FilingHistoryDeltaTimestamp()
                .at(Instant.now()).by("context_id");

        // when
        FilingHistoryDeltaTimestamp actual = DateUtils.makeNewTimeStampObject(Instant.now(), "context_id");

        //then
        assertEquals(expected, actual);
    }
}