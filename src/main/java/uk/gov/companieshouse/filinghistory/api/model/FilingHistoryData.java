package uk.gov.companieshouse.filinghistory.api.model;

import java.time.Instant;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class FilingHistoryData {

    private String type;
    private Instant date;
    private String category;
    private String subcategory;
    private String description;
    @Field("description_values")
    private FilingHistoryDescriptionValues descriptionValues;
    @Field("action_date")
    private Instant actionDate;
    private Integer pages;
    @Field("paper_filed")
    private Boolean paperFiled;
    private FilingHistoryLinks links;

    public String getType() {
        return type;
    }

    public FilingHistoryData type(String type) {
        this.type = type;
        return this;
    }

    public Instant getDate() {
        return date;
    }

    public FilingHistoryData date(Instant date) {
        this.date = date;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public FilingHistoryData category(String category) {
        this.category = category;
        return this;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public FilingHistoryData subcategory(String subcategory) {
        this.subcategory = subcategory;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public FilingHistoryData description(String description) {
        this.description = description;
        return this;
    }

    public FilingHistoryDescriptionValues getDescriptionValues() {
        return descriptionValues;
    }

    public FilingHistoryData descriptionValues(FilingHistoryDescriptionValues descriptionValues) {
        this.descriptionValues = descriptionValues;
        return this;
    }

    public Instant getActionDate() {
        return actionDate;
    }

    public FilingHistoryData actionDate(Instant actionDate) {
        this.actionDate = actionDate;
        return this;
    }

    public Integer getPages() {
        return pages;
    }

    public FilingHistoryData pages(Integer pages) {
        this.pages = pages;
        return this;
    }

    public Boolean getPaperFiled() {
        return paperFiled;
    }

    public FilingHistoryData paperFiled(Boolean paperFiled) {
        this.paperFiled = paperFiled;
        return this;
    }

    public FilingHistoryLinks getLinks() {
        return links;
    }

    public FilingHistoryData links(FilingHistoryLinks links) {
        this.links = links;
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
        FilingHistoryData that = (FilingHistoryData) o;
        return Objects.equals(type, that.type) && Objects.equals(date, that.date) && Objects.equals(category, that.category) && Objects.equals(subcategory, that.subcategory) && Objects.equals(description, that.description) && Objects.equals(descriptionValues, that.descriptionValues) && Objects.equals(actionDate, that.actionDate) && Objects.equals(pages, that.pages) && Objects.equals(paperFiled, that.paperFiled) && Objects.equals(links, that.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, date, category, subcategory, description, descriptionValues, actionDate, pages, paperFiled, links);
    }
}
