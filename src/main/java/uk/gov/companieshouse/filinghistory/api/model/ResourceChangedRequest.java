package uk.gov.companieshouse.filinghistory.api.model;

import java.util.List;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

public record ResourceChangedRequest(FilingHistoryDocument document, String companyNumber, String transactionId,
                                     boolean isDelete, List<String> fieldsChanged) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private FilingHistoryDocument document;
        private String companyNumber;
        private String transactionId;
        private boolean isDelete;
        private List<String> fieldsChanged;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder document(FilingHistoryDocument document) {
            this.document = document;
            return this;
        }

        public Builder companyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder isDelete(boolean isDelete) {
            this.isDelete = isDelete;
            return this;
        }

        public Builder fieldsChanged(List<String> fieldsChanged) {
            this.fieldsChanged = fieldsChanged;
            return this;
        }

        public ResourceChangedRequest build() {
            return new ResourceChangedRequest(document, companyNumber, transactionId, isDelete, fieldsChanged);
        }
    }
}
