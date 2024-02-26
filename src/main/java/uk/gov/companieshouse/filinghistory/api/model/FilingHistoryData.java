package uk.gov.companieshouse.filinghistory.api.model;

import java.time.Instant;
import java.util.List;
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
    private List<FilingHistoryAnnotation> annotations;
    private List<FilingHistoryResolution> resolutions;
    @Field("associated_filings")
    private List<FilingHistoryAssociatedFiling> associatedFilings;
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

    public List<FilingHistoryAnnotation> getAnnotations() {
        return annotations;
    }

    public FilingHistoryData annotations(List<FilingHistoryAnnotation> annotations) {
        this.annotations = annotations;
        return this;
    }

    public List<FilingHistoryResolution> getResolutions() {
        return resolutions;
    }

    public FilingHistoryData resolutions(List<FilingHistoryResolution> resolutions) {
        this.resolutions = resolutions;
        return this;
    }

    public List<FilingHistoryAssociatedFiling> getAssociatedFilings() {
        return associatedFilings;
    }

    public FilingHistoryData associatedFilings(List<FilingHistoryAssociatedFiling> associatedFilings) {
        this.associatedFilings = associatedFilings;
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
        return Objects.equals(type, that.type) && Objects.equals(date, that.date) && Objects.equals(category,
                that.category) && Objects.equals(subcategory, that.subcategory) && Objects.equals(description,
                that.description) && Objects.equals(descriptionValues, that.descriptionValues) && Objects.equals(
                annotations, that.annotations) && Objects.equals(resolutions, that.resolutions) && Objects.equals(
                associatedFilings, that.associatedFilings) && Objects.equals(actionDate, that.actionDate)
                && Objects.equals(pages, that.pages) && Objects.equals(paperFiled, that.paperFiled) && Objects.equals(
                links, that.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, date, category, subcategory, description, descriptionValues, annotations, resolutions,
                associatedFilings, actionDate, pages, paperFiled, links);
    }

    @Override
    public String toString() {
        return "FilingHistoryData{" +
                "type='" + type + '\'' +
                ", date=" + date +
                ", category='" + category + '\'' +
                ", subcategory='" + subcategory + '\'' +
                ", description='" + description + '\'' +
                ", descriptionValues=" + descriptionValues +
                ", annotations=" + annotations +
                ", resolutions=" + resolutions +
                ", associatedFilings=" + associatedFilings +
                ", actionDate=" + actionDate +
                ", pages=" + pages +
                ", paperFiled=" + paperFiled +
                ", links=" + links +
                '}';
    }
}