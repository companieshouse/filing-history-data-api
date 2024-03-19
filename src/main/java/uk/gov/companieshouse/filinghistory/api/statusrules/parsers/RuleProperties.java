package uk.gov.companieshouse.filinghistory.api.statusrules.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RuleProperties(@JsonProperty("filing-history") FilingHistoryProperties filingHistory) {

}
