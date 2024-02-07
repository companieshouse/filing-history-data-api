package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.filinghistory.api.client.ResourceChangedApiClient;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.repository.Repository;

@SpringBootTest
@ActiveProfiles("resource-changed-call-disabled")
class HandleResourceChangedAspectFeatureDisabledIT {

    @Autowired
    private FilingHistoryService filingHistoryService;
    @MockBean
    private ResourceChangedApiClient resourceChangedApiClient;
    @MockBean
    private Repository repository;
    @Mock
    FilingHistoryDocument filingHistoryDocument;

    @Test
    void testThatAspectShouldNotProceedWhenFeatureDisabled() {
        // given

        // when
        ServiceResult result = filingHistoryService.insertFilingHistory(filingHistoryDocument);

        // then
        assertEquals(ServiceResult.UPSERT_SUCCESSFUL, result);
        verify(repository).save(filingHistoryDocument);
        verifyNoInteractions(resourceChangedApiClient);
    }
}
