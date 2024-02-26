package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.AltCapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.CapitalDescriptionValue;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAltCapital;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryCapital;

class CapitalDescriptionMapperTest {

    private final CapitalDescriptionMapper mapper = new CapitalDescriptionMapper();

    @Test
    void mapCapitalDescriptionValueList() {

        CapitalDescriptionValue capitalDescriptionValue = new CapitalDescriptionValue()
                .currency("GBP")
                .figure("100.00")
                .date("2022-01-01T00:00:00.00Z");
        FilingHistoryCapital expected = new FilingHistoryCapital()
                .currency("GBP")
                .figure("100.00")
                .date("2022-01-01T00:00:00.00Z");

        List<FilingHistoryCapital> actual = mapper.mapCapitalDescriptionValueList(List.of(capitalDescriptionValue));
        assertEquals(1, actual.size());
        assertEquals(expected, actual.getFirst());
    }

    @Test
    void mapAltCapitalDescriptionValueList() {

        AltCapitalDescriptionValue altCapitalDescriptionValue = new AltCapitalDescriptionValue()
                .currency("GBP")
                .figure("100.00")
                .date("2022-01-01T00:00:00.00Z")
                .description("description");

        FilingHistoryAltCapital expected = new FilingHistoryAltCapital()
                .currency("GBP")
                .figure("100.00")
                .date("2022-01-01T00:00:00.00Z")
                .description("description");

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