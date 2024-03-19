package uk.gov.companieshouse.filinghistory.api.statusrules.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PrefixProperties(@JsonProperty("type") String type,
                               @JsonProperty("status") String status,
                               @JsonProperty("from") FromProperties from) {

}
