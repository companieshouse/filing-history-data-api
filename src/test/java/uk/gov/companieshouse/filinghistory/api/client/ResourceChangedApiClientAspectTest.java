package uk.gov.companieshouse.filinghistory.api.client;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceChangedApiClientAspectTest {

    @InjectMocks
    private ResourceChangedApiClientAspect apiServiceAspect;

    @Test
    void testAspectDoesNotProceedWhenFlagDisabled() {
        // when
        Object actual = apiServiceAspect.checkStreamEventsEnabled();

        // then
        assertNull(actual);
    }
}
