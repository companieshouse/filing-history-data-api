package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.filinghistory.api.service.CompanyNumberStatusProcessor.CompanyNumberAffixes;
import uk.gov.companieshouse.filinghistory.api.statusrules.PrefixProperties;
import uk.gov.companieshouse.filinghistory.api.statusrules.StatusRuleProperties;

@ExtendWith(MockitoExtension.class)
class FilingHistoryStatusServiceTest {

    @InjectMocks
    private FilingHistoryStatusService filingHistoryStatusService;

    @Mock
    private StatusRuleProperties statusRuleProperties;
    @Mock
    private CompanyNumberStatusProcessor companyNumberStatusProcessor;

    @Test
    void shouldSuccessfullyProcessStatus() {
        // given
        PrefixProperties prefixProperties = new PrefixProperties(
                "type",
                "status",
                null);

        final String prefix = "AB";
        final String suffix = "123456";
        final String companyNumber = prefix + suffix;

        CompanyNumberAffixes companyNumberAffixes = new CompanyNumberAffixes(prefix, suffix);

        when(companyNumberStatusProcessor.splitCompanyNumberAffixes(any())).thenReturn(companyNumberAffixes);
        when(statusRuleProperties.filingHistory()).thenReturn(Map.of(prefix, prefixProperties));
        when(companyNumberStatusProcessor.getStatusFromPrefixProperties(any(), any())).thenReturn(prefixProperties.status());

        final String expected = "status";

        // when
        final String actual = filingHistoryStatusService.processStatus(companyNumber);

        // then
        assertEquals(expected, actual);
    }
}
