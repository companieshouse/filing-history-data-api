package uk.gov.companieshouse.filinghistory.api.model.mongo;

import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class FilingHistoryIds {

    private List<String> ids;

    public List<String> getIds() {
        return ids;
    }

    public FilingHistoryIds ids(List<String> ids) {
        this.ids = ids;
        return this;
    }
}
