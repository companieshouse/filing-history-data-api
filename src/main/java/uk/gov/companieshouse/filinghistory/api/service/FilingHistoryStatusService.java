package uk.gov.companieshouse.filinghistory.api.service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.gov.companieshouse.filinghistory.api.statusrules.FromProperties;
import uk.gov.companieshouse.filinghistory.api.statusrules.PrefixProperties;
import uk.gov.companieshouse.filinghistory.api.statusrules.StatusRuleProperties;

public class FilingHistoryStatusService implements StatusService {

    private final StatusRuleProperties statusRuleProperties;

    public FilingHistoryStatusService(StatusRuleProperties statusRuleProperties) {
        this.statusRuleProperties = statusRuleProperties;
    }

    @Override
    public String processStatus(String companyNumber) {
        Pattern pattern = Pattern.compile("^([A-Z]{2}|R0|)(\\d+)");
        Matcher matcher = pattern.matcher(companyNumber);

        final String prefix;
        if (matcher.find()) {
            prefix = matcher.group(1).isEmpty() ? "NORMAL" : matcher.group(1);
        } else {
            prefix = "INVALID_FORMAT";
        }

        Map<String, PrefixProperties> filingHistory = statusRuleProperties.filingHistory();
        PrefixProperties prefixProperties = filingHistory.getOrDefault(prefix, filingHistory.get("UNKNOWN_PREFIX"));

        String status = prefixProperties.status();
        if (prefixProperties.from() != null) {
            for (FromProperties fromProperties : prefixProperties.from()) {
                if (fromProperties.number() > Integer.parseInt(matcher.group(2))) {
                    break;
                }
                status = fromProperties.status();
            }
        }
        return status;
    }
}

