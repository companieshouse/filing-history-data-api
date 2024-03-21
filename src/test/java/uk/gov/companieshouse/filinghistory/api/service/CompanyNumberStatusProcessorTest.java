package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.companieshouse.filinghistory.api.statusrules.FromProperties;
import uk.gov.companieshouse.filinghistory.api.statusrules.PrefixProperties;

class CompanyNumberStatusProcessorTest {

    private final CompanyNumberStatusProcessor companyNumberStatusProcessor = new CompanyNumberStatusProcessor();

    @ParameterizedTest
    @CsvSource({
            "AB123456 , AB",
            "12345678 , NORMAL",
            "ABC12345 , INVALID_FORMAT"
    })
    void shouldSetPrefixFromMatcher(final String companyNumber, final String expected) {
        // given
        Matcher matcher = Pattern.compile("^([A-Z]{2}|R0|)(\\d+)").matcher(companyNumber);

        // when
        final String actual = companyNumberStatusProcessor.getPrefixFromRegexMatch(matcher);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldSetStatusFromPrefixPropertiesWithNoFromProperties() {
        // given
        Matcher matcher = Pattern.compile("^([A-Z]{2}|R0|)(\\d+)").matcher("AB123456");
        PrefixProperties prefixProperties = new PrefixProperties("type", "status", null);
        final String expected = "status";

        // when
        final String actual = companyNumberStatusProcessor.getStatusFromPrefixProperties(prefixProperties, matcher);

        // then
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "LP000999 , status",
            "LP001999 , from_status_one",
            "LP999999 , from_status_two"
    })
    void shouldSetStatusFromPrefixPropertiesWithFromProperties(final String companyNumber, final String expected) {
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
        final String actual = companyNumberStatusProcessor.getStatusFromPrefixProperties(prefixProperties, matcher);

        // then
        assertTrue(matchFound);
        assertEquals(expected, actual);
    }
}
