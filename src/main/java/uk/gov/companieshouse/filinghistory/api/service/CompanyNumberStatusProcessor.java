package uk.gov.companieshouse.filinghistory.api.service;

import java.util.regex.Matcher;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.statusrules.FromProperties;
import uk.gov.companieshouse.filinghistory.api.statusrules.PrefixProperties;

@Component
public class CompanyNumberStatusProcessor {

    public String getPrefixFromRegexMatch(Matcher matcher) {
        final String prefix;
        if (matcher.find()) {
            prefix = matcher.group(1).isEmpty() ? "NORMAL" : matcher.group(1);
        } else {
            prefix = "INVALID_FORMAT";
        }
        return prefix;
    }

    public String getStatusFromPrefixProperties(PrefixProperties prefixProperties, Matcher matcher) {
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
