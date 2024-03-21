package uk.gov.companieshouse.filinghistory.api.service;

import java.util.Map;
import uk.gov.companieshouse.filinghistory.api.model.statusrules.PrefixProperties;
import uk.gov.companieshouse.filinghistory.api.model.statusrules.StatusRuleProperties;
import uk.gov.companieshouse.filinghistory.api.service.CompanyNumberStatusProcessor.CompanyNumberAffixes;

public class FilingHistoryStatusService implements StatusService {

    private final StatusRuleProperties statusRuleProperties;
    private final CompanyNumberStatusProcessor companyNumberStatusProcessor;

    public FilingHistoryStatusService(StatusRuleProperties statusRuleProperties,
                                      CompanyNumberStatusProcessor companyNumberStatusProcessor) {
        this.statusRuleProperties = statusRuleProperties;
        this.companyNumberStatusProcessor = companyNumberStatusProcessor;
    }

    @Override
    public String processStatus(String companyNumber) {
        CompanyNumberAffixes companyNumberAffixes = companyNumberStatusProcessor
                .splitCompanyNumberAffixes(companyNumber);

        Map<String, PrefixProperties> filingHistory = statusRuleProperties.filingHistory();
        PrefixProperties prefixProperties = filingHistory
                .getOrDefault(companyNumberAffixes.prefix(), filingHistory.get("UNKNOWN_PREFIX"));

        return companyNumberStatusProcessor
                .getStatusFromPrefixProperties(prefixProperties, companyNumberAffixes.suffix());
    }
}

