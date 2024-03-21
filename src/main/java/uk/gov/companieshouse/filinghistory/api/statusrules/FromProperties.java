package uk.gov.companieshouse.filinghistory.api.statusrules;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FromProperties(@JsonProperty("number") String number,
                             @JsonProperty("status") String status) {
}
