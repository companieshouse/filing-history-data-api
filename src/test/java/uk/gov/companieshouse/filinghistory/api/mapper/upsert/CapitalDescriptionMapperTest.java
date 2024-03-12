package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.AltCapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.CapitalDescriptionValue;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAltCapital;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryCapital;

class CapitalDescriptionMapperTest {

    private static final String DATE_STRING = "2022-01-01T00:00:00.00Z";
    private static final Instant DATE_INSTANT = Instant.parse(DATE_STRING);
    private static final String CURRENCY = "GBP";
    private static final String FIGURE = "100.00";
    private static final String ALT_DESCRIPTION = "alt description";
    private final CapitalDescriptionMapper mapper = new CapitalDescriptionMapper();

    @Test
    void mapCapitalDescriptionValueList() {

        CapitalDescriptionValue capitalDescriptionValue = new CapitalDescriptionValue()
                .currency(CURRENCY)
                .figure(FIGURE)
                .date(DATE_STRING);
        FilingHistoryCapital expected = new FilingHistoryCapital()
                .currency(CURRENCY)
                .figure(FIGURE)
                .date(DATE_INSTANT);

        List<FilingHistoryCapital> actual = mapper.mapCapitalDescriptionValueList(List.of(capitalDescriptionValue));
        assertEquals(1, actual.size());
        assertEquals(expected, actual.getFirst());
    }

    @Test
    void mapAltCapitalDescriptionValueList() {

        AltCapitalDescriptionValue altCapitalDescriptionValue = new AltCapitalDescriptionValue()
                .currency(CURRENCY)
                .figure(FIGURE)
                .date(DATE_STRING)
                .description(ALT_DESCRIPTION);

        FilingHistoryAltCapital expected = new FilingHistoryAltCapital()
                .currency(CURRENCY)
                .figure(FIGURE)
                .date(DATE_INSTANT)
                .description(ALT_DESCRIPTION);

        List<FilingHistoryAltCapital> actual = mapper.mapAltCapitalDescriptionValueList(
                List.of(altCapitalDescriptionValue));
        assertEquals(1, actual.size());
        assertEquals(expected, actual.getFirst());
    }


    @Test
    void shouldMapNullCapitalDescriptionValueListToNull() {
        // given

        // when
        List<FilingHistoryCapital> actual = mapper.mapCapitalDescriptionValueList(null);

        // then
        assertNull(actual);
    }

    @Test
    void shouldMapNullAltCapitalDescriptionValueListToNull() {
        // given

        // when
        List<FilingHistoryAltCapital> actual = mapper.mapAltCapitalDescriptionValueList(null);

        // then
        assertNull(actual);
    }
}