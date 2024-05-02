package uk.gov.companieshouse.filinghistory.api.model.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class FilingHistoryResolution extends FilingHistoryChild {

    private String barcode;
    private String category;
    private String description;
    private String type;
    private Object subcategory;
    private Instant date;
    @Field("original_description")
    @JsonProperty("original_description")
    private String originalDescription;
    @Field("description_values")
    @JsonProperty("description_values")
    private FilingHistoryDescriptionValues descriptionValues;

    @Override
    public FilingHistoryResolution entityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    @Override
    public FilingHistoryResolution deltaAt(String deltaAt) {
        this.deltaAt = deltaAt;
        return this;
    }

    public String getBarcode() {
        return barcode;
    }

    public FilingHistoryResolution barcode(String barcode) {
        this.barcode = barcode;
        return this;
    }

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

    public Object getSubcategory() {
        return subcategory;
    }

    public FilingHistoryResolution subcategory(Object subcategory) {
        this.subcategory = subcategory;
        return this;
    }

    public Instant getDate() {
        return date;
    }

    public FilingHistoryResolution date(Instant date) {
        this.date = date;
        return this;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }

    public FilingHistoryResolution originalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
        return this;
    }

    public FilingHistoryDescriptionValues getDescriptionValues() {
        return descriptionValues;
    }

    public FilingHistoryResolution descriptionValues(
            FilingHistoryDescriptionValues descriptionValues) {
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
        if (!super.equals(o)) {
            return false;
        }
        FilingHistoryResolution that = (FilingHistoryResolution) o;
        return Objects.equals(barcode, that.barcode) && Objects.equals(category, that.category)
                && Objects.equals(description, that.description) && Objects.equals(type, that.type)
                && Objects.equals(subcategory, that.subcategory) && Objects.equals(date, that.date)
                && Objects.equals(originalDescription, that.originalDescription) && Objects.equals(
                descriptionValues, that.descriptionValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), barcode, category, description, type, subcategory, date,
                originalDescription,
                descriptionValues);
    }

    @Override
    public String toString() {
        return "FilingHistoryResolution{" +
                "barcode='" + barcode + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", subcategory=" + subcategory +
                ", date=" + date +
                ", originalDescription='" + originalDescription + '\'' +
                ", descriptionValues=" + descriptionValues +
                "} " + super.toString();
    }
}
