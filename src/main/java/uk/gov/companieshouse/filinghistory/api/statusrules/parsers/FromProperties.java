package uk.gov.companieshouse.filinghistory.api.statusrules.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FromProperties(@JsonProperty("number") int number,
                             @JsonProperty("status") String status) {
}
