package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.AltCapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.CapitalDescriptionValue;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAltCapital;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryCapital;

class CapitalDescriptionGetResponseMapperTest {

    private static final String DATE_STRING = "2022-01-01";
    private static final Instant DATE_INSTANT = Instant.parse(DATE_STRING + "T00:00:00.00Z");
    private final CapitalDescriptionGetResponseMapper mapper = new CapitalDescriptionGetResponseMapper();

    @Test
    void shouldMapCapitalDescriptionList() {
        FilingHistoryCapital filingHistoryCapital = new FilingHistoryCapital()
                .currency("GBP")
                .figure("100.00")
                .date(DATE_INSTANT);
        CapitalDescriptionValue expected = new CapitalDescriptionValue()
                .currency("GBP")
                .figure("100.00")
                .date(DATE_STRING);

        List<CapitalDescriptionValue> actual = mapper.mapFilingHistoryCapital(List.of(filingHistoryCapital));
        assertEquals(1, actual.size());
        assertEquals(expected, actual.getFirst());
    }

    @Test
    void shouldMapAltCapitalDescriptionList() {
        FilingHistoryAltCapital filingHistoryCapital = new FilingHistoryAltCapital()
                .currency("GBP")
                .figure("100.00")
                .date(DATE_INSTANT)
                .description("alt capital");
        AltCapitalDescriptionValue expected = new AltCapitalDescriptionValue()
                .currency("GBP")
                .figure("100.00")
                .date(DATE_STRING)
                .description("alt capital");

        List<AltCapitalDescriptionValue> actual = mapper.mapFilingHistoryAltCapital(List.of(filingHistoryCapital));
        assertEquals(1, actual.size());
        assertEquals(expected, actual.getFirst());
    }

    @Test
    void shouldMapNullCapitalDescriptionList() {
        // given

        // when
        List<CapitalDescriptionValue> actual = mapper.mapFilingHistoryCapital(null);

        // then
        assertNull(actual);
    }

    @Test
    void shouldMapNullAltCapitalDescriptionList() {
        // given

        // when
        List<AltCapitalDescriptionValue> actual = mapper.mapFilingHistoryAltCapital(null);

        // then
        assertNull(actual);
    }
}