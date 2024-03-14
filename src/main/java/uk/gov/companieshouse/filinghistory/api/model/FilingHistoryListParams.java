package uk.gov.companieshouse.filinghistory.api.model;

import java.util.List;

public record FilingHistoryListParams(String companyNumber, Integer startIndex, Integer itemsPerPage, List<String> categories) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String companyNumber;
        private Integer startIndex;
        private Integer itemsPerPage;
        private List<String> categories;

        private Builder() {
        }

        public Builder companyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder startIndex(Integer startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public Builder itemsPerPage(Integer itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
            return this;
        }

        public Builder categories(List<String> categories) {
            this.categories = categories;
            return this;
        }

        public FilingHistoryListParams build() {
            return new FilingHistoryListParams(companyNumber, startIndex, itemsPerPage, categories);
        }
    }
}
