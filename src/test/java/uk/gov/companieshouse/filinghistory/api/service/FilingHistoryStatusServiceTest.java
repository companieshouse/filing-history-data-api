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

        when(companyNumberStatusProcessor.getPrefixFromRegexMatch(any())).thenReturn("AB");
        when(statusRuleProperties.filingHistory()).thenReturn(Map.of("AB", prefixProperties));
        when(companyNumberStatusProcessor.getStatusFromPrefixProperties(any(), any())).thenReturn(prefixProperties.status());

        final String expected = "status";

        // when
        final String actual = filingHistoryStatusService.processStatus("AB123456");

        // then
        assertEquals(expected, actual);
    }
}
