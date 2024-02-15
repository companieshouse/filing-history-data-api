package uk.gov.companieshouse.filinghistory.api.client;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.ResourceChangedRequestMapper;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class ResourceChangedApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String CHANGED_RESOURCE_URI = "/private/resource-changed";
    private final Supplier<InternalApiClient> apiClientSupplier;
    private final ResourceChangedRequestMapper mapper;

    public ResourceChangedApiClient(ResourceChangedRequestMapper mapper,
            Supplier<InternalApiClient> apiClientSupplier) {
        this.mapper = mapper;
        this.apiClientSupplier = apiClientSupplier;
    }

    @CallResourceChanged
    public ApiResponse<Void> callResourceChanged(ResourceChangedRequest resourceChangedRequest) {
        InternalApiClient internalApiClient = apiClientSupplier.get();
        internalApiClient.getHttpClient().setRequestId(DataMapHolder.getRequestId());

        try {
            return internalApiClient
                    .privateChangedResourceHandler()
                    .postChangedResource(CHANGED_RESOURCE_URI, mapper.mapChangedResource(resourceChangedRequest))
                    .execute();
        } catch (ApiErrorResponseException ex) {
            LOGGER.error("Unsuccessful call to /resource-changed endpoint", ex, DataMapHolder.getLogMap());
            return new ApiResponse<>(ex.getStatusCode(), ex.getHeaders());
        }
    }
}
