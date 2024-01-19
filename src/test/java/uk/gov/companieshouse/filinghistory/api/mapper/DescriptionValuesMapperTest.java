package uk.gov.companieshouse.filinghistory.api.mapper;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;

class DescriptionValuesMapperTest {

    private static final String OFFICER_NAME = "John Tester";
    private static final LocalDate TERMINATION_DATE = LocalDate.of(2020, 6, 10);


    private final DescriptionValuesMapper mapper = new DescriptionValuesMapper();

    @Test
    void shouldMapDescriptionValues() {
        // given
        FilingHistoryItemDataDescriptionValues descriptionValues = new FilingHistoryItemDataDescriptionValues()
                .officerName(OFFICER_NAME)
                .terminationDate(TERMINATION_DATE);

        FilingHistoryDescriptionValues expected = new FilingHistoryDescriptionValues()
                .officerName(OFFICER_NAME)
                .terminationDate(Instant.from(TERMINATION_DATE.atStartOfDay(UTC)));

        // when
        FilingHistoryDescriptionValues actual = mapper.map(descriptionValues);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnNullWhenDescriptionValuesNull() {
        // given

        // when
        FilingHistoryDescriptionValues actual = mapper.map(null);

        // then
        assertNull(actual);
    }

}