package uk.gov.companieshouse.filinghistory.api.model;

import java.time.Instant;
import java.util.Objects;

public abstract class AbstractFilingHistoryChild {

    protected String category;
    protected String description;
    protected String type;
    protected Instant date;
    protected FilingHistoryDescriptionValues descriptionValues;

    public String getCategory() {
        return category;
    }

    public AbstractFilingHistoryChild category(String category) {
        this.category = category;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AbstractFilingHistoryChild description(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public AbstractFilingHistoryChild type(String type) {
        this.type = type;
        return this;
    }

    public Instant getDate() {
        return date;
    }

    public AbstractFilingHistoryChild date(Instant date) {
        this.date = date;
        return this;
    }

    public FilingHistoryDescriptionValues getDescriptionValues() {
        return descriptionValues;
    }

    public AbstractFilingHistoryChild descriptionValues(FilingHistoryDescriptionValues descriptionValues) {
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
        AbstractFilingHistoryChild that = (AbstractFilingHistoryChild) o;
        return Objects.equals(category, that.category) && Objects.equals(description, that.description) && Objects.equals(type, that.type) && Objects.equals(date, that.date) && Objects.equals(descriptionValues, that.descriptionValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, description, type, date, descriptionValues);
    }
}
