package uk.gov.companieshouse.filinghistory.api.model;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Objects;

public class FilingHistoryOriginalValues {

    @Field("officer_name")
    private String officerName;
    @Field("resignation_date")
    private String resignationDate;

    public String getOfficerName() {
        return officerName;
    }

    public FilingHistoryOriginalValues officerName(String officerName) {
        this.officerName = officerName;
        return this;
    }

    public String getResignationDate() {
        return resignationDate;
    }

    public FilingHistoryOriginalValues resignationDate(String resignationDate) {
        this.resignationDate = resignationDate;
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
        FilingHistoryOriginalValues that = (FilingHistoryOriginalValues) o;
        return Objects.equals(officerName, that.officerName) && Objects.equals(resignationDate, that.resignationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(officerName, resignationDate);
    }
}
