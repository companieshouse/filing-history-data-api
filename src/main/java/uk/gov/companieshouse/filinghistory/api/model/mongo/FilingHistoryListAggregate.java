package uk.gov.companieshouse.filinghistory.api.model.mongo;

import java.util.List;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

public class FilingHistoryListAggregate {

    private long totalCount;
    private List<FilingHistoryDocument> documentList;

    public long getTotalCount() {
        return totalCount;
    }

    public FilingHistoryListAggregate totalCount(long totalCount) {
        this.totalCount = totalCount;
        return this;
    }

    public List<FilingHistoryDocument> getDocumentList() {
        return documentList;
    }

    public FilingHistoryListAggregate documentList(List<FilingHistoryDocument> documentList) {
        this.documentList = documentList;
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
        FilingHistoryListAggregate that = (FilingHistoryListAggregate) o;
        return totalCount == that.totalCount && Objects.equals(documentList, that.documentList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalCount, documentList);
    }

    @Override
    public String toString() {
        return "FilingHistoryListAggregate{" +
                "totalCount=" + totalCount +
                ", documentList=" + documentList +
                '}';
    }
}
