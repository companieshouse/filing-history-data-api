package uk.gov.companieshouse.filinghistory.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Field;

public class FilingHistoryLinks {

    private String self;
    @Field("document_metadata")
    @JsonProperty("document_metadata")
    private String documentMetadata;

    public String getSelf() {
        return self;
    }

    public FilingHistoryLinks self(String self) {
        this.self = self;
        return this;
    }

    public String getDocumentMetadata() {
        return documentMetadata;
    }

    public FilingHistoryLinks documentMetadata(String documentMetadata) {
        this.documentMetadata = documentMetadata;
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
        FilingHistoryLinks that = (FilingHistoryLinks) o;
        return Objects.equals(self, that.self) && Objects.equals(documentMetadata, that.documentMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(self, documentMetadata);
    }

    @Override
    public String toString() {
        return "FilingHistoryLinks{" +
                "self='" + self + '\'' +
                ", documentMetadata='" + documentMetadata + '\'' +
                '}';
    }
}
