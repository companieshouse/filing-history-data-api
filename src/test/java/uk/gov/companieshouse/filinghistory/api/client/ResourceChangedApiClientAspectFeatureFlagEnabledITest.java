package uk.gov.companieshouse.filinghistory.api.client;

import static org.mockito.Mockito.verifyNoInteractions;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;

@SpringBootTest
@ActiveProfiles("feature_flag_enabled")
class ResourceChangedApiClientAspectFeatureFlagEnabledITest {
    @Autowired
    private ResourceChangedApiClient resourceChangedApiClient;
    @MockBean
    private Supplier<InternalApiClient> internalApiClientSupplier;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private ResourceChangedRequest resourceChangedRequest;
    @Mock
    private PrivateChangedResourceHandler privateChangedResourceHandler;
    @Mock
    private PrivateChangedResourcePost changedResourcePost;

    @Test
    void testThatAspectShouldNotProceedWhenFeatureFlagEnabled() throws ApiErrorResponseException {

        resourceChangedApiClient.invokeChsKafkaApi(resourceChangedRequest);

        verifyNoInteractions(internalApiClientSupplier);
        verifyNoInteractions(internalApiClient);
        verifyNoInteractions(privateChangedResourceHandler);
        verifyNoInteractions(changedResourcePost);
    }
}
