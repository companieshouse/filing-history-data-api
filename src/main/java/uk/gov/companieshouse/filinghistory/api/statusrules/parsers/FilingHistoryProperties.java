package uk.gov.companieshouse.filinghistory.api.statusrules.parsers;

import java.util.Map;

public record FilingHistoryProperties(Map<String, PrefixProperties> prefix) {
}
