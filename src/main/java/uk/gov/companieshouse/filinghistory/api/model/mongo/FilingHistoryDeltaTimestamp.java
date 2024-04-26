package uk.gov.companieshouse.filinghistory.api.model.mongo;

import java.time.Instant;
import java.util.Objects;

public class FilingHistoryDeltaTimestamp {

    private Instant at;
    private String by;

    public FilingHistoryDeltaTimestamp(Instant at, String by) {
        this.at = at;
        this.by = by;
    }

    public Instant getAt() {
        return at;
    }

    public FilingHistoryDeltaTimestamp at(Instant at) {
        this.at = at;
        return this;
    }

    public String getBy() {
        return by;
    }

    public FilingHistoryDeltaTimestamp by(String by) {
        this.by = by;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FilingHistoryDeltaTimestamp that = (FilingHistoryDeltaTimestamp) o;
        return Objects.equals(at, that.at) && Objects.equals(by, that.by);
    }

    @Override
    public int hashCode() {
        return Objects.hash(at, by);
    }
}
