package uk.gov.companieshouse.filinghistory.api.service;

import java.util.Optional;
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

        final String companyNumberPrefix = matcher.find() ? matcher.group(1) : null;
        final int extractedNumber = Integer.parseInt(matcher.group(2));

        return Optional.ofNullable(companyNumberPrefix)
                .map(prefix -> {
                    if (prefix.isEmpty()) {
                        // If prefix == ""
                        return getPrefixProperties("NORMAL").status();
                    }
                    return getStatusFromValidPrefix(prefix, extractedNumber);
                })
                .orElse(getPrefixProperties("INVALID_FORMAT").status());
    }

    private PrefixProperties getPrefixProperties(final String prefix) {
        return statusRuleProperties
                .filingHistory()
                .getOrDefault(prefix, null);
    }

    private String getStatusFromValidPrefix(final String validPrefix, final int number) {
        // If prefix is not null nor empty
        return Optional.ofNullable(getPrefixProperties(validPrefix))
                // If a prefix property exists for prefix
                .map(prefixProperties -> {
                    // If prefixProperties contains a 'from' array
                    if (prefixProperties.from() != null) {
                        for (FromProperties fromProperties : prefixProperties.from()) {
                            if (fromProperties.number() > number) {
                                return fromProperties.status();
                            }
                        }
                    }
                    // If prefixProperties does not contain a 'from' array
                    return prefixProperties.status();
                })
                // If a prefix property does not exist for prefix
                .orElse(getPrefixProperties("UNKNOWN_PREFIX").status());
    }
}

