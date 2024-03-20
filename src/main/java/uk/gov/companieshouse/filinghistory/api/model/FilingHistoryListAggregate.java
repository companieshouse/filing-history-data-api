package uk.gov.companieshouse.filinghistory.api.model;

import java.util.List;
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
}
