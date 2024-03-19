package uk.gov.companieshouse.filinghistory.api.service;

import uk.gov.companieshouse.filinghistory.api.statusrules.parsers.PrefixProperties;
import uk.gov.companieshouse.filinghistory.api.statusrules.parsers.RuleProperties;

public class FilingHistoryStatusService implements StatusService {

    private final RuleProperties ruleProperties;

    public FilingHistoryStatusService(RuleProperties ruleProperties) {
        this.ruleProperties = ruleProperties;
    }

    @Override
    public String processStatus(String companyNumber) {
        final String companyNumberPrefix = companyNumber.substring(0, 2);
        PrefixProperties prefixProperties = ruleProperties.filingHistory().prefix().get(companyNumberPrefix);
        if (prefixProperties != null) {

        }
        return null;
    }
}
