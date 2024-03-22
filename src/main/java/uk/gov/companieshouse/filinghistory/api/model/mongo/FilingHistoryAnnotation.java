package uk.gov.companieshouse.filinghistory.api.model.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class FilingHistoryAnnotation {

    private String annotation;
    private String category;
    private String description;
    private String type;
    private Instant date;
    @Field("_entity_id")
    @JsonProperty("_entity_id")
    private String entityId;
    @Field("description_values")
    @JsonProperty("description_values")
    private FilingHistoryDescriptionValues descriptionValues;

    @Field("delta_at")
    @JsonProperty("delta_at")
    private String deltaAt;

    public String getEntityId() {
        return entityId;
    }

    public FilingHistoryAnnotation entityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    public String getAnnotation() {
        return annotation;
    }

    public FilingHistoryAnnotation annotation(String annotation) {
        this.annotation = annotation;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public FilingHistoryAnnotation category(String category) {
        this.category = category;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public FilingHistoryAnnotation description(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public FilingHistoryAnnotation type(String type) {
        this.type = type;
        return this;
    }

    public Instant getDate() {
        return date;
    }

    public FilingHistoryAnnotation date(Instant date) {
        this.date = date;
        return this;
    }

    public FilingHistoryDescriptionValues getDescriptionValues() {
        return descriptionValues;
    }

    public FilingHistoryAnnotation descriptionValues(FilingHistoryDescriptionValues descriptionValues) {
        this.descriptionValues = descriptionValues;
        return this;
    }

    public String getDeltaAt() {
        return deltaAt;
    }

    public FilingHistoryAnnotation deltaAt(String deltaAt) {
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
        FilingHistoryAnnotation that = (FilingHistoryAnnotation) o;
        return Objects.equals(annotation, that.annotation) && Objects.equals(category, that.category) && Objects.equals(description, that.description) && Objects.equals(type, that.type) && Objects.equals(date, that.date) && Objects.equals(entityId, that.entityId) && Objects.equals(descriptionValues, that.descriptionValues) && Objects.equals(deltaAt, that.deltaAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotation, category, description, type, date, entityId, descriptionValues, deltaAt);
    }

    @Override
    public String toString() {
        return "FilingHistoryAnnotation{" +
                "annotation='" + annotation + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", date=" + date +
                ", entityId='" + entityId + '\'' +
                ", descriptionValues=" + descriptionValues +
                ", deltaAt='" + deltaAt + '\'' +
                '}';
    }
}
