package uk.gov.companieshouse.filinghistory.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.ResourceChangedRequestMapper;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;

@SpringBootTest
class ResourceChangedApiClientAspectFeatureDisabledIT {

    @Autowired
    private ResourceChangedApiClient client;
    @MockBean
    private Supplier<InternalApiClient> apiClientSupplier;
    @MockBean
    private ResourceChangedRequestMapper mapper;
    @Mock
    private ResourceChangedRequest resourceChangedRequest;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("feature.resource_changed_call.disabled", () -> true);
    }

    @Test
    void shouldNotCallResourceChangedWhenFeatureDisabled() {
        // given

        // when
        ApiResponse<Void> actual = client.callResourceChanged(resourceChangedRequest);

        // then
        assertEquals(200, actual.getStatusCode());
        verifyNoInteractions(apiClientSupplier);
    }
}
