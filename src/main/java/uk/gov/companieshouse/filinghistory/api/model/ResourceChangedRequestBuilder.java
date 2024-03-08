package uk.gov.companieshouse.filinghistory.api.model;

public class ResourceChangedRequestBuilder {

    private String contextId;
    private String companyNumber;
    private String transactionId;
    private Object filingHistoryData;
    private boolean isDelete;

    private ResourceChangedRequestBuilder() {
    }

    public static ResourceChangedRequestBuilder builder() {
        return new ResourceChangedRequestBuilder();
    }

    public String getContextId() {
        return contextId;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Object getFilingHistoryData() {
        return filingHistoryData;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public ResourceChangedRequestBuilder contextId(String contextId) {
        this.contextId = contextId;
        return this;
    }

    public ResourceChangedRequestBuilder companyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public ResourceChangedRequestBuilder transactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public ResourceChangedRequestBuilder filingHistoryData(Object filingHistoryData) {
        this.filingHistoryData = filingHistoryData;
        return this;
    }

    public ResourceChangedRequestBuilder isDelete(boolean isDelete) {
        this.isDelete = isDelete;
        return this;
    }

    public ResourceChangedRequest build() {
        return new ResourceChangedRequest(this);
    }
}


