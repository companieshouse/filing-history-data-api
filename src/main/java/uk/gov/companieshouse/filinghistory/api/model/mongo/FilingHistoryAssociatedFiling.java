package uk.gov.companieshouse.filinghistory.api.model.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class FilingHistoryAssociatedFiling {

    private String category;
    private String description;
    private String type;
    private Instant date;
    @Field("description_values")
    @JsonProperty("description_values")
    private FilingHistoryDescriptionValues descriptionValues;
    @Field("_entity_id")
    @JsonProperty("_entity_id")
    private String entityId;
    @Field("delta_at")
    @JsonProperty("delta_at")
    private String deltaAt;
    @Field("original_description")
    @JsonProperty("original_description")
    private String originalDescription;

    public String getCategory() {
        return category;
    }

    public FilingHistoryAssociatedFiling category(String category) {
        this.category = category;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public FilingHistoryAssociatedFiling description(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public FilingHistoryAssociatedFiling type(String type) {
        this.type = type;
        return this;
    }

    public Instant getDate() {
        return date;
    }

    public FilingHistoryAssociatedFiling date(Instant date) {
        this.date = date;
        return this;
    }

    public FilingHistoryDescriptionValues getDescriptionValues() {
        return descriptionValues;
    }

    public FilingHistoryAssociatedFiling descriptionValues(FilingHistoryDescriptionValues descriptionValues) {
        this.descriptionValues = descriptionValues;
        return this;
    }

    public String getEntityId() {
        return entityId;
    }

    public FilingHistoryAssociatedFiling entityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    public String getDeltaAt() {
        return deltaAt;
    }

    public FilingHistoryAssociatedFiling deltaAt(String deltaAt) {
        this.deltaAt = deltaAt;
        return this;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }

    public FilingHistoryAssociatedFiling originalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
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
        FilingHistoryAssociatedFiling that = (FilingHistoryAssociatedFiling) o;
        return Objects.equals(category, that.category) && Objects.equals(description, that.description) && Objects.equals(type, that.type) && Objects.equals(date, that.date) && Objects.equals(descriptionValues, that.descriptionValues) && Objects.equals(entityId, that.entityId) && Objects.equals(deltaAt, that.deltaAt) && Objects.equals(originalDescription, that.originalDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, description, type, date, descriptionValues, entityId, deltaAt, originalDescription);
    }

    @Override
    public String toString() {
        return "FilingHistoryAssociatedFiling{" +
                "category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", date=" + date +
                ", descriptionValues=" + descriptionValues +
                ", entityId='" + entityId + '\'' +
                ", deltaAt='" + deltaAt + '\'' +
                ", originalDescription='" + originalDescription + '\'' +
                '}';
    }
}
