package uk.gov.companieshouse.filinghistory.api.service;

public enum ServiceResult {
    UPSERT_SUCCESSFUL,
    STALE_DELTA,
    SERVICE_UNAVAILABLE,
    BAD_REQUEST
}
