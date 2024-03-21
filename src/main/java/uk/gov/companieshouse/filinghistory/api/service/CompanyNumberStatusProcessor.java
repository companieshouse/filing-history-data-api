package uk.gov.companieshouse.filinghistory.api.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.statusrules.FromProperties;
import uk.gov.companieshouse.filinghistory.api.statusrules.PrefixProperties;

@Component
public class CompanyNumberStatusProcessor {

    private static final Pattern COMPANY_NUMBER_PATTERN = Pattern.compile("^([A-Z]{2}|R0|)(\\d+)");

    public CompanyNumberAffixes splitCompanyNumberAffixes(final String companyNumber) {
        Matcher matcher = COMPANY_NUMBER_PATTERN.matcher(companyNumber);

        final String prefix;
        final String suffix;
        if (matcher.find()) {
            prefix = matcher.group(1).isEmpty() ? "NORMAL" : matcher.group(1);
            suffix = matcher.group(2);
        } else {
            prefix = "INVALID_FORMAT";
            suffix = "";
        }
        return new CompanyNumberAffixes(prefix, suffix);
    }

    public String getStatusFromPrefixProperties(PrefixProperties prefixProperties, final String companyNumberSuffix) {
        String status = prefixProperties.status();
        if (prefixProperties.from() != null) {
            for (FromProperties fromProperties : prefixProperties.from()) {
                if (isGreaterThan(fromProperties.number(), companyNumberSuffix)) {
                    break;
                }
                status = fromProperties.status();
            }
        }
        return status;
    }

    private static boolean isGreaterThan(final String fromNumber, final String companyNumberSuffix) {
        return Integer.parseInt(fromNumber) > Integer.parseInt(companyNumberSuffix);
    }

    public record CompanyNumberAffixes(String prefix, String suffix) {

    }
}
