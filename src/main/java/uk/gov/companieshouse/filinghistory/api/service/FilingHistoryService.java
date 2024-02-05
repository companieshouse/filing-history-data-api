package uk.gov.companieshouse.filinghistory.api.service;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.filinghistory.api.client.ResourceChangedApiService;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;
import uk.gov.companieshouse.filinghistory.api.repository.Repository;

@Component
public class FilingHistoryService implements Service {
    private final ResourceChangedApiService resourceChangedApiService;

    private final Repository repository;

    public FilingHistoryService(ResourceChangedApiService resourceChangedApiService, Repository repository) {
        this.resourceChangedApiService = resourceChangedApiService;
        this.repository = repository;
    }

    public Optional<FilingHistoryDocument> findExistingFilingHistory(final String transactionId) {
        return repository.findById(transactionId);
    }

    public ServiceResult insertFilingHistory(final FilingHistoryDocument documentToSave) {
        repository.save(documentToSave);
        try {
            // TODO: compensatory transaction as part of DSND-2280.
            resourceChangedApiService.invokeChsKafkaApi(
                    new ResourceChangedRequest(DataMapHolder.getRequestId(), documentToSave.getCompanyNumber(),
                            documentToSave.getTransactionId(), null, false));
        } catch (ApiErrorResponseException e) {
            // TODO: exception catching as part of DSND-2280.
            throw new RuntimeException(e);
        }
        return ServiceResult.UPSERT_SUCCESSFUL;
    }

    @Override
    public ServiceResult updateFilingHistory(FilingHistoryDocument documentToSave,
            FilingHistoryDocument existingDocument) {
        repository.save(documentToSave);
        try {
            // TODO: compensatory transaction as part of DSND-2280.
            resourceChangedApiService.invokeChsKafkaApi(
                    new ResourceChangedRequest(DataMapHolder.getRequestId(), documentToSave.getCompanyNumber(),
                            documentToSave.getTransactionId(), null, false));
        } catch (ApiErrorResponseException e) {
            // TODO: exception catching as part of DSND-2280.
            throw new RuntimeException(e);
        }
        return ServiceResult.UPSERT_SUCCESSFUL;
    }
}
