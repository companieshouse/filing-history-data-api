package uk.gov.companieshouse.filinghistory.api.client;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.mapper.ResourceChangedRequestMapper;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class ResourceChangedApiService {

    private static final String CHANGED_RESOURCE_URI = "/resource-changed";
    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final String chsKafkaUrl;
    private final ApiClientService apiClientService;
    private final ResourceChangedRequestMapper mapper;

    /**
     * Invoke API.
     */
    public ResourceChangedApiService(@Value("${chs.kafka.api.endpoint}") String chsKafkaUrl,
                                ApiClientService apiClientService,
                                ResourceChangedRequestMapper mapper) {
        this.chsKafkaUrl = chsKafkaUrl;
        this.apiClientService = apiClientService;
        this.mapper = mapper;
    }


    /**
     * Calls the CHS Kafka api.
     * @param resourceChangedRequest encapsulates details relating to the updated or deleted company exemption
     * @return The service status of the response from chs kafka api
     */
    @StreamEvents
    public ApiResponse<Void> invokeChsKafkaApi(ResourceChangedRequest resourceChangedRequest) throws ServiceUnavailableException {
        InternalApiClient internalApiClient = apiClientService.getInternalApiClient(); //NOSONAR
        internalApiClient.getHttpClient().setRequestId(DataMapHolder.getRequestId());

        internalApiClient.setBasePath(chsKafkaUrl);

        PrivateChangedResourcePost changedResourcePost =
                internalApiClient.privateChangedResourceHandler().postChangedResource(
                        CHANGED_RESOURCE_URI, mapper.mapChangedResource(resourceChangedRequest));

        return handleApiCall(changedResourcePost);
    }

    private ApiResponse<Void> handleApiCall(PrivateChangedResourcePost changedResourcePost) throws ServiceUnavailableException {
        try {
            return changedResourcePost.execute();
        } catch (ApiErrorResponseException ex) {
            if (!HttpStatus.valueOf(ex.getStatusCode()).is2xxSuccessful()) {
                LOGGER.error("Unsuccessful call to /resource-changed endpoint", ex, DataMapHolder.getLogMap());
            } else {
                LOGGER.error("Error occurred while calling /resource-changed endpoint", ex, DataMapHolder.getLogMap());
            }
            throw new ServiceUnavailableException(ex.getMessage());
        }
    }
}
