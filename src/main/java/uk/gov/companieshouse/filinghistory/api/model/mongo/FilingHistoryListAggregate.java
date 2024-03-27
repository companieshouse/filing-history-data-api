package uk.gov.companieshouse.filinghistory.api.model.mongo;

import java.util.List;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class FilingHistoryListAggregate {

    @Field("total_count")
    private Integer totalCount;
    @Field("document_list")
    private List<FilingHistoryDocument> documentList;

    public Integer getTotalCount() {
        return totalCount;
    }

    public FilingHistoryListAggregate totalCount(Integer totalCount) {
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
        return Objects.equals(totalCount, that.totalCount) && Objects.equals(
                documentList, that.documentList);
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
