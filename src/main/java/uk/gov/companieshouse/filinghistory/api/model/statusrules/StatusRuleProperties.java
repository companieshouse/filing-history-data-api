package uk.gov.companieshouse.filinghistory.api.model.statusrules;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record StatusRuleProperties(@JsonProperty("filing-history") Map<String, PrefixProperties> filingHistory) {

}
