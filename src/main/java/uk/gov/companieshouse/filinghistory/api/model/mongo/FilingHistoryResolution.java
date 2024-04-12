package uk.gov.companieshouse.filinghistory.api.model.mongo;

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
    @Field("original_description")
    @JsonProperty("original_description")
    private String originalDescription;
    private String subcategory;
    @Field("delta_at")
    @JsonProperty("delta_at")
    private String deltaAt;

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

    public String getOriginalDescription() {
        return originalDescription;
    }

    public FilingHistoryResolution originalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
        return this;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public FilingHistoryResolution subcategory(String subcategory) {
        this.subcategory = subcategory;
        return this;
    }

    public String getDeltaAt() {
        return deltaAt;
    }

    public FilingHistoryResolution deltaAt(String deltaAt) {
        this.deltaAt = deltaAt;
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
        return Objects.equals(category, that.category) && Objects.equals(description, that.description) && Objects.equals(type, that.type) && Objects.equals(date, that.date) && Objects.equals(descriptionValues, that.descriptionValues) && Objects.equals(originalDescription, that.originalDescription) && Objects.equals(subcategory, that.subcategory) && Objects.equals(deltaAt, that.deltaAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, description, type, date, descriptionValues, originalDescription, subcategory, deltaAt);
    }

    @Override
    public String toString() {
        return "FilingHistoryResolution{" +
                "category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", date=" + date +
                ", descriptionValues=" + descriptionValues +
                ", originalDescription='" + originalDescription + '\'' +
                ", subcategory='" + subcategory + '\'' +
                ", deltaAt='" + deltaAt + '\'' +
                '}';
    }
}
