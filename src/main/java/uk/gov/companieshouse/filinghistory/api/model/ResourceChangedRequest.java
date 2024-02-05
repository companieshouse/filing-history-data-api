package uk.gov.companieshouse.filinghistory.api.model;

import java.util.Objects;

public class ResourceChangedRequest {

    private final String contextId;
    private final String companyNumber;
    private final String transactionId;
    private final Object filingHistoryData;
    private final Boolean isDelete;

    public ResourceChangedRequest(String contextId, String companyNumber, String transactionId,
                                  Object filingHistoryData, Boolean isDelete) {
        this.contextId = contextId;
        this.companyNumber = companyNumber;
        this.transactionId = transactionId;
        this.filingHistoryData = filingHistoryData;
        this.isDelete = isDelete;
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

    public Boolean getIsDelete() {
        return isDelete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResourceChangedRequest that = (ResourceChangedRequest) o;
        return Objects.equals(contextId, that.contextId) &&
                Objects.equals(companyNumber, that.companyNumber) &&
                Objects.equals(transactionId, that.transactionId) &&
                Objects.equals(filingHistoryData, that.filingHistoryData) &&
                Objects.equals(isDelete, that.isDelete);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextId, companyNumber, transactionId, filingHistoryData, isDelete);
    }
}
