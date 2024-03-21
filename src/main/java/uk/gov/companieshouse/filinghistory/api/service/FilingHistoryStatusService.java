package uk.gov.companieshouse.filinghistory.api.service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.gov.companieshouse.filinghistory.api.statusrules.PrefixProperties;
import uk.gov.companieshouse.filinghistory.api.statusrules.StatusRuleProperties;

public class FilingHistoryStatusService implements StatusService {

    private final StatusRuleProperties statusRuleProperties;
    private final CompanyNumberStatusProcessor companyNumberStatusProcessor;

    public FilingHistoryStatusService(StatusRuleProperties statusRuleProperties, CompanyNumberStatusProcessor companyNumberStatusProcessor) {
        this.statusRuleProperties = statusRuleProperties;
        this.companyNumberStatusProcessor = companyNumberStatusProcessor;
    }

    @Override
    public String processStatus(String companyNumber) {
        Pattern pattern = Pattern.compile("^([A-Z]{2}|R0|)(\\d+)");
        Matcher matcher = pattern.matcher(companyNumber);

        final String prefix = companyNumberStatusProcessor.getPrefixFromRegexMatch(matcher);

        Map<String, PrefixProperties> filingHistory = statusRuleProperties.filingHistory();
        PrefixProperties prefixProperties = filingHistory.getOrDefault(prefix, filingHistory.get("UNKNOWN_PREFIX"));

        return companyNumberStatusProcessor.getStatusFromPrefixProperties(prefixProperties, matcher);
    }
}

