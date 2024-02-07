package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.filinghistory.api.service.HandleResourceChangedAspect;

@ExtendWith(MockitoExtension.class)
class HandleResourceChangedAspectTest {

    @InjectMocks
    private HandleResourceChangedAspect apiServiceAspect;

    @Test
    void testAspectDoesNotProceedWhenFlagDisabled() {
        // when
        Object actual = apiServiceAspect.resourceChangedCallDisabled();

        // then
        assertNull(actual);
    }
}
