package uk.gov.companieshouse.filinghistory.api.model;

public record FilingHistoryDeleteRequest(String companyNumber, String transactionId, String entityId, String deltaAt,
                                         String parentEntityId) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String companyNumber;
        private String transactionId;
        private String entityId;
        private String deltaAt;
        private String parentEntityId;

        public Builder companyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder deltaAt(String deltaAt) {
            this.deltaAt = deltaAt;
            return this;
        }

        public Builder parentEntityId(String parentEntityId) {
            this.parentEntityId = parentEntityId;
            return this;
        }

        public FilingHistoryDeleteRequest build() {
            return new FilingHistoryDeleteRequest(companyNumber, transactionId, entityId, deltaAt, parentEntityId);
        }
    }
}
