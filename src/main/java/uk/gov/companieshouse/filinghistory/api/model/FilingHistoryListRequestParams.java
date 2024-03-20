package uk.gov.companieshouse.filinghistory.api.model;

import java.util.List;

public record FilingHistoryListRequestParams(String companyNumber, int startIndex, int itemsPerPage,
                                             List<String> categories) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String companyNumber;
        private int startIndex;
        private int itemsPerPage;
        private List<String> categories;

        private Builder() {
        }

        public Builder companyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder startIndex(int startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public Builder itemsPerPage(int itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
            return this;
        }

        public Builder categories(List<String> categories) {
            this.categories = categories;
            return this;
        }

        public FilingHistoryListRequestParams build() {
            return new FilingHistoryListRequestParams(companyNumber, startIndex, itemsPerPage, categories);
        }
    }
}
