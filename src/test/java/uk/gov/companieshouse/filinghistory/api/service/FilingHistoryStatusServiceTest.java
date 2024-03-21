package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FilingHistoryStatusServiceTest {

    @Autowired
    private FilingHistoryStatusService filingHistoryStatusService;

    @ParameterizedTest
    @CsvSource({
            "AC999999 , filing-history-not-available-assurance-company-from-2004",
            "AC001838 , filing-history-available-assurance-company-before-2004",
            "BR999999 , filing-history-available",
            "ES999999 , filing-history-available",
            "FC999999 , filing-history-available",
            "FE999999 , filing-history-available",
            "GE999999 , filing-history-available",
            "GN999999 , filing-history-available",
            "GS999999 , filing-history-available",
            "IC999999 , filing-history-not-available-investment-company-with-variable-capital",
            "IP999999 , filing-history-not-available-industrial-and-provident-society",
            "LP999999 , filing-history-available-limited-partnership-from-2014",
            "LP003369 , filing-history-not-available-limited-partnership-before-1988",
            "LP015996 , filing-history-available-no-images-limited-partnership-from-1988",
            "NA999999 , filing-history-not-available-assurance-company-from-2004",
            "NA000001 , filing-history-not-available-assurance-company-from-2004",
            "NC999999 , filing-history-available",
            "NF999999 , filing-history-available",
            "NI999999 , filing-history-available",
            "NL999999 , filing-history-available-limited-partnership-from-2014",
            "NL000010 , filing-history-not-available-limited-partnership-before-1988",
            "NL000075 , filing-history-available-no-images-limited-partnership-from-1988",
            "NO999999 , filing-history-not-available-northern-ireland-other-industrial-and-provident-society",
            "NP999999 , filing-history-not-available-northern-ireland-industrial-and-provident-society",
            "NR999999 , filing-history-not-available-royal-charter",
            "NV999999 , filing-history-not-available-investment-company-with-variable-capital",
            "NZ999999 , filing-history-available",
            "OC999999 , filing-history-available",
            "OE999999 , filing-history-available",
            "PC999999 , filing-history-not-available-protected-cell-company",
            "R0999999 , filing-history-available",
            "RC999999 , filing-history-not-available-royal-charter",
            "SA999999 , filing-history-not-available-assurance-company-from-2004",
            "SA144517 , filing-history-available-assurance-company-before-2004",
            "SC999999 , filing-history-available",
            "SE999999 , filing-history-available",
            "SF999999 , filing-history-available",
            "SG999999 , filing-history-available",
            "SI999999 , filing-history-not-available-investment-company-with-variable-capital",
            "SL999999 , filing-history-available-limited-partnership-from-2014",
            "SL001082 , filing-history-not-available-limited-partnership-before-1988",
            "SL016383 , filing-history-available-no-images-limited-partnership-from-1988",
            "SO999999 , filing-history-available",
            "SP999999 , filing-history-not-available-scottish-industrial-and-provident-society",
            "SR999999 , filing-history-not-available-royal-charter",
            "SZ999999 , filing-history-available",
            "ZC999999 , filing-history-available",
            "12345678 , filing-history-available",
            "AA999999 , filing-history-not-available-unknown-prefix",
            "ABC12345 , filing-history-not-available-invalid-format"
    })
    void shouldCorrectlyReturnStatusFromGivenCompanyNumber(final String companyNumber, final String expected) {
        // given

        // when
        final String actual = filingHistoryStatusService.processStatus(companyNumber);

        // then
        assertEquals(expected, actual);
    }
}
