package uk.gov.companieshouse.filinghistory.api.model.mongo;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class FilingHistoryDeleteAggregate {

    @Field("resolution_index")
    private int resolutionIndex = -1;
    @Field("annotation_index")
    private int annotationIndex = -1;
    @Field("associated_filing_index")
    private int associatedFilingIndex = -1;
    @Field("document")
    private FilingHistoryDocument document;

    public int getResolutionIndex() {
        return resolutionIndex;
    }

    public FilingHistoryDeleteAggregate resolutionIndex(int resolutionIndex) {
        this.resolutionIndex = resolutionIndex;
        return this;
    }

    public int getAnnotationIndex() {
        return annotationIndex;
    }

    public FilingHistoryDeleteAggregate annotationIndex(int annotationIndex) {
        this.annotationIndex = annotationIndex;
        return this;
    }

    public int getAssociatedFilingIndex() {
        return associatedFilingIndex;
    }

    public FilingHistoryDeleteAggregate associatedFilingIndex(int associatedFilingIndex) {
        this.associatedFilingIndex = associatedFilingIndex;
        return this;
    }

    public FilingHistoryDocument getDocument() {
        return document;
    }

    public FilingHistoryDeleteAggregate document(FilingHistoryDocument document) {
        this.document = document;
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
        FilingHistoryDeleteAggregate aggregate = (FilingHistoryDeleteAggregate) o;
        return resolutionIndex == aggregate.resolutionIndex && annotationIndex == aggregate.annotationIndex
                && associatedFilingIndex == aggregate.associatedFilingIndex && Objects.equals(document,
                aggregate.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resolutionIndex, annotationIndex, associatedFilingIndex, document);
    }

    @Override
    public String toString() {
        return "FilingHistoryDeleteAggregate{" +
                "resolutionIndex=" + resolutionIndex +
                ", annotationIndex=" + annotationIndex +
                ", associatedFilingIndex=" + associatedFilingIndex +
                ", document=" + document +
                '}';
    }
}
