package uk.gov.companieshouse.filinghistory.api.client;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.mapper.ResourceChangedRequestMapper;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class ResourceChangedApiClient {

    private static final String CHANGED_RESOURCE_URI = "/resource-changed";
    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final Supplier<InternalApiClient> internalApiClientFactory;
    private final ResourceChangedRequestMapper mapper;

    /**
     * Invoke API.
     */
    public ResourceChangedApiClient(ResourceChangedRequestMapper mapper, Supplier<InternalApiClient> internalApiClientFactory) {
        this.mapper = mapper;
        this.internalApiClientFactory = internalApiClientFactory;
    }

    public ApiResponse<Void> callResourceChanged(ResourceChangedRequest resourceChangedRequest) {
        InternalApiClient internalApiClient = internalApiClientFactory.get();
        internalApiClient.getHttpClient().setRequestId(DataMapHolder.getRequestId());

        PrivateChangedResourcePost changedResourcePost =
                internalApiClient.privateChangedResourceHandler().postChangedResource(
                        CHANGED_RESOURCE_URI, mapper.mapChangedResource(resourceChangedRequest));
        try {
            return changedResourcePost.execute();
        } catch (ApiErrorResponseException e) {
            LOGGER.error("Unsuccessful call to /resource-changed endpoint", e, DataMapHolder.getLogMap());
            return new ApiResponse<>(e.getStatusCode(), e.getHeaders());
        }
    }
}
