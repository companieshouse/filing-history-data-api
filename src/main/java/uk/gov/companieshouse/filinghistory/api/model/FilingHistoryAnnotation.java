package uk.gov.companieshouse.filinghistory.api.model;

import java.util.Objects;

public class FilingHistoryAnnotation extends AbstractFilingHistoryChild {

    private String annotation;

    public String getAnnotation() {
        return annotation;
    }

    public FilingHistoryAnnotation annotation(String annotation) {
        this.annotation = annotation;
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
        FilingHistoryAnnotation that = (FilingHistoryAnnotation) o;
        return Objects.equals(annotation, that.annotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), annotation);
    }
}
