package uk.gov.companieshouse.filinghistory.api.service;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.filinghistory.api.client.ResourceChangedApiClient;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;
import uk.gov.companieshouse.filinghistory.api.repository.Repository;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class FilingHistoryService implements Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final ResourceChangedApiClient apiClient;
    private final Repository repository;

    public FilingHistoryService(ResourceChangedApiClient apiClient, Repository repository) {
        this.apiClient = apiClient;
        this.repository = repository;
    }

    public Optional<FilingHistoryDocument> findExistingFilingHistory(final String transactionId) {
        return repository.findById(transactionId);
    }

    public ServiceResult insertFilingHistory(final FilingHistoryDocument documentToSave) {
        return handleTransaction(documentToSave, null);
    }

    @Override
    public ServiceResult updateFilingHistory(FilingHistoryDocument documentToSave,
                                             FilingHistoryDocument existingDocument) {
        return handleTransaction(documentToSave, existingDocument);
    }

    private ServiceResult handleTransaction(FilingHistoryDocument documentToSave,
                                            FilingHistoryDocument existingDocument) {
        // Add compensatory transaction as part of DSND-2280.
        try {
            repository.save(documentToSave);
        } catch (ServiceUnavailableException ex) {
            return ServiceResult.SERVICE_UNAVAILABLE;
        }

        ApiResponse<Void> result = apiClient.callResourceChanged(
                new ResourceChangedRequest(DataMapHolder.getRequestId(), documentToSave.getCompanyNumber(),
                        documentToSave.getTransactionId(), null, false));

        if (!HttpStatus.valueOf(result.getStatusCode()).is2xxSuccessful()) {
            LOGGER.error("Call to resource-changed endpoint was not 200 OK", DataMapHolder.getLogMap());
            return ServiceResult.SERVICE_UNAVAILABLE;
        }
        return ServiceResult.UPSERT_SUCCESSFUL;
    }
}
