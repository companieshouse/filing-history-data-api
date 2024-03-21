package uk.gov.companieshouse.filinghistory.api.model.statusrules;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FromProperties(@JsonProperty("number") String number,
                             @JsonProperty("status") String status) {
}
