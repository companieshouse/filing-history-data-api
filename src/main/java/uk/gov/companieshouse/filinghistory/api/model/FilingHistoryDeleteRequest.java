package uk.gov.companieshouse.filinghistory.api.model;

public record FilingHistoryDeleteRequest (String companyNumber, String transactionId, String entityId, String deltaAt) {

}
