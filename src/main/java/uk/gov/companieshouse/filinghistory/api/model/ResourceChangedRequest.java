package uk.gov.companieshouse.filinghistory.api.model;

public record ResourceChangedRequest(String contextId, String companyNumber, String transactionId,
                                     Object filingHistoryData, Boolean isDelete) {

    public ResourceChangedRequest(ResourceChangedRequestBuilder builder) {
        this(builder.getContextId(), builder.getCompanyNumber(), builder.getTransactionId(),
                builder.getFilingHistoryData(), builder.isDelete());
    }
}
