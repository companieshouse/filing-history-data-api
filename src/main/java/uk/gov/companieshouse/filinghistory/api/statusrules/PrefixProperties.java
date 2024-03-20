package uk.gov.companieshouse.filinghistory.api.statusrules;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PrefixProperties(@JsonProperty("type") String type,
                               @JsonProperty("status") String status,
                               @JsonProperty("from") List<FromProperties> from) {

}
