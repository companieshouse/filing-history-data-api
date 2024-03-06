package uk.gov.companieshouse.filinghistory.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class FilingHistoryResolution {

    private String category;
    private String description;
    private String type;
    private Instant date;
    @Field("description_values")
	@JsonProperty("description_values")
    private FilingHistoryDescriptionValues descriptionValues;

    public String getCategory() {
        return category;
    }

    public FilingHistoryResolution category(String category) {
        this.category = category;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public FilingHistoryResolution description(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public FilingHistoryResolution type(String type) {
        this.type = type;
        return this;
    }

    public Instant getDate() {
        return date;
    }

    public FilingHistoryResolution date(Instant date) {
        this.date = date;
        return this;
    }

    public FilingHistoryDescriptionValues getDescriptionValues() {
        return descriptionValues;
    }

    public FilingHistoryResolution descriptionValues(FilingHistoryDescriptionValues descriptionValues) {
        this.descriptionValues = descriptionValues;
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
        FilingHistoryResolution that = (FilingHistoryResolution) o;
        return Objects.equals(category, that.category) && Objects.equals(description, that.description)
                && Objects.equals(type, that.type) && Objects.equals(date, that.date) && Objects.equals(
                descriptionValues, that.descriptionValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, description, type, date, descriptionValues);
    }
}
