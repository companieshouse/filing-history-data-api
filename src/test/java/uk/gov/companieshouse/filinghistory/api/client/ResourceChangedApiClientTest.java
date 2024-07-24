package uk.gov.companieshouse.filinghistory.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.ResourceChangedRequestMapper;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;

@ExtendWith(MockitoExtension.class)
class ResourceChangedApiClientTest {

    @Mock
    private Supplier<InternalApiClient> apiClientSupplier;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private PrivateChangedResourceHandler privateChangedResourceHandler;

    @Mock
    private PrivateChangedResourcePost changedResourcePost;

    @Mock
    private ApiResponse<Void> response;

    @Mock
    private ResourceChangedRequestMapper mapper;

    @Mock
    private ResourceChangedRequest resourceChangedRequest;

    @Mock
    private ChangedResource changedResource;

    @Mock
    private HttpClient httpClient;

    @InjectMocks
    private ResourceChangedApiClient resourceChangedApiClient;

    @BeforeEach
    void setup() {
        when(internalApiClient.getHttpClient()).thenReturn(httpClient);
    }

    @Test
    void shouldCallPostChangedResourceAndReturnOk() throws ApiErrorResponseException {
        // given
        when(apiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(changedResourcePost);
        when(changedResourcePost.execute()).thenReturn(response);
        when(mapper.mapChangedResource(resourceChangedRequest)).thenReturn(changedResource);

        // when
        resourceChangedApiClient.callResourceChanged(resourceChangedRequest);

        // then
        verify(apiClientSupplier).get();
        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler).postChangedResource("/private/resource-changed", changedResource);
        verify(changedResourcePost).execute();
    }

    @Test
    void shouldCallPostChangedResourceAndReturnBadGatewayWhenApiErrorResponse502()
            throws ApiErrorResponseException {
        // given
        HttpResponseException.Builder builder = new HttpResponseException.Builder(502, "Bad Gateway",
                new HttpHeaders());
        ApiErrorResponseException apiErrorResponseException = new ApiErrorResponseException(builder);

        when(apiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(changedResourcePost);
        when(mapper.mapChangedResource(resourceChangedRequest)).thenReturn(changedResource);
        when(changedResourcePost.execute()).thenThrow(apiErrorResponseException);

        // when
        ApiResponse<Void> result = resourceChangedApiClient.callResourceChanged(resourceChangedRequest);

        // then
        assertEquals(502, result.getStatusCode());
        verify(apiClientSupplier).get();
        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler).postChangedResource("/private/resource-changed", changedResource);
        verify(changedResourcePost).execute();
    }
}
