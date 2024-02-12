package uk.gov.companieshouse.filinghistory.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;

public class DescriptionValuesResponseMapperTest {

    private final DescriptionValuesResponseMapper mapper = new DescriptionValuesResponseMapper();

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

    private static FilingHistoryDescriptionValues buildDocumentDescriptionValues() {
        return new FilingHistoryDescriptionValues()
                .officerName("John Tester")
                .terminationDate(Instant.parse("2014-08-29T00:00:00.000Z"));
    }
}
