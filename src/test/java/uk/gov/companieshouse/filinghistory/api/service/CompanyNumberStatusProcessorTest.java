package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.companieshouse.filinghistory.api.service.CompanyNumberStatusProcessor.CompanyNumberAffixes;
import uk.gov.companieshouse.filinghistory.api.statusrules.FromProperties;
import uk.gov.companieshouse.filinghistory.api.statusrules.PrefixProperties;

class CompanyNumberStatusProcessorTest {

    private final CompanyNumberStatusProcessor companyNumberStatusProcessor = new CompanyNumberStatusProcessor();

    @ParameterizedTest
    @CsvSource({
            "AB123456 , AB , 123456",
            "12345678 , NORMAL , 12345678",
            "ABC12345 , INVALID_FORMAT , ''"
    })
    void shouldSplitCompanyNumberAffixes(final String companyNumber, final String expectedPrefix, final String expectedSuffix) {
        // given
        final CompanyNumberAffixes expected = new CompanyNumberAffixes(expectedPrefix, expectedSuffix);

        // when
        final CompanyNumberAffixes actual = companyNumberStatusProcessor.splitCompanyNumberAffixes(companyNumber);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldSetStatusFromPrefixPropertiesWithNoFromProperties() {
        // given
        PrefixProperties prefixProperties = new PrefixProperties("type", "status", null);
        final String expected = "status";

        // when
        final String actual = companyNumberStatusProcessor.getStatusFromPrefixProperties(prefixProperties, "123456");

        // then
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "LP000999 , status , 000999",
            "LP001999 , from_status_one , 001999",
            "LP999999 , from_status_two , 999999"
    })
    void shouldSetStatusFromPrefixPropertiesWithFromProperties(final String companyNumber, final String expected, final String suffix) {
        // given
        Matcher matcher = Pattern.compile("^([A-Z]{2}|R0|)(\\d+)").matcher(companyNumber);
        final boolean matchFound = matcher.find();
        PrefixProperties prefixProperties = new PrefixProperties(
                "type",
                "status",
                List.of(new FromProperties(
                                "1000",
                                "from_status_one"),
                        new FromProperties(
                                "2000",
                                "from_status_two")));

        // when
        final String actual = companyNumberStatusProcessor.getStatusFromPrefixProperties(prefixProperties, suffix);

        // then
        assertTrue(matchFound);
        assertEquals(expected, actual);
    }
}
