package uk.gov.companieshouse.filinghistory.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.model.ApiResponse;

class ResourceChangedApiClientAspectTest {

    private final ResourceChangedApiClientAspect apiServiceAspect = new ResourceChangedApiClientAspect();

    @Test
    void shouldReturnOkWhenFeatureDisabled() {
        // when
        ApiResponse<Void> actual = apiServiceAspect.callResourceChangedDisabled();

        // then
        assertEquals(200, actual.getStatusCode());
    }
}
