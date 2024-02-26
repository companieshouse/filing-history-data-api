package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;

class DescriptionValuesGetResponseMapperTest {

    private final DescriptionValuesGetResponseMapper mapper = new DescriptionValuesGetResponseMapper();

    @Test
    void shouldSuccessfullyMapDescriptionValues() {
        // given
        final FilingHistoryItemDataDescriptionValues expected = new FilingHistoryItemDataDescriptionValues()
                .officerName("John Tester")
                .terminationDate("2014-08-29");

        // when
        final FilingHistoryItemDataDescriptionValues actual = mapper.map(buildDocumentDescriptionValues());

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnNullWhenPassedNullDescriptionValues() {
        // given

        // when
        final FilingHistoryItemDataDescriptionValues actual = mapper.map(null);

        // then
        assertNull(actual);
    }

    private static FilingHistoryDescriptionValues buildDocumentDescriptionValues() {
        return new FilingHistoryDescriptionValues()
                .officerName("John Tester")
                .terminationDate(Instant.parse("2014-08-29T00:00:00.000Z"));
    }
}
