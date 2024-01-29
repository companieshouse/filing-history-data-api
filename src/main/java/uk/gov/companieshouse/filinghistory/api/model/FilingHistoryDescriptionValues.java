package uk.gov.companieshouse.filinghistory.api.model;

import java.time.Instant;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class FilingHistoryDescriptionValues {

    @Field("termination_date")
    private Instant terminationDate;
    @Field("officer_name")
    private String officerName;

    public Instant getTerminationDate() {
        return terminationDate;
    }

    public FilingHistoryDescriptionValues terminationDate(Instant terminationDate) {
        this.terminationDate = terminationDate;
        return this;
    }

    public String getOfficerName() {
        return officerName;
    }

    public FilingHistoryDescriptionValues officerName(String officerName) {
        this.officerName = officerName;
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
        FilingHistoryDescriptionValues that = (FilingHistoryDescriptionValues) o;
        return Objects.equals(terminationDate, that.terminationDate) && Objects.equals(officerName, that.officerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(terminationDate, officerName);
    }

    @Override
    public String toString() {
        return "FilingHistoryDescriptionValues{" +
                "terminationDate=" + terminationDate +
                ", officerName='" + officerName + '\'' +
                '}';
    }
}
