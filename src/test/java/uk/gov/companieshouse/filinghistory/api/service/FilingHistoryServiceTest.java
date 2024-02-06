package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.filinghistory.api.client.ResourceChangedApiClient;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.repository.Repository;

@ExtendWith(MockitoExtension.class)
class FilingHistoryServiceTest {

    private static final String TRANSACTION_ID = "transactionId";

    @InjectMocks
    private FilingHistoryService service;
    @Mock
    private ResourceChangedApiClient resourceChangedApiClient;
    @Mock
    private Repository repository;
    @Mock
    private FilingHistoryDocument document;
    @Mock
    private ApiResponse<Void> response;

    @Test
    void findExistingFilingHistoryDocumentShouldReturnDocument() {
        // given
        when(repository.findById(any())).thenReturn(Optional.of(document));

        // when
        final Optional<FilingHistoryDocument> actualDocument = service.findExistingFilingHistory(TRANSACTION_ID);

        // then
        assertTrue(actualDocument.isPresent());
        verify(repository).findById(TRANSACTION_ID);
    }

    @Test
    void findExistingFilingHistoryDocumentShouldReturnEmptyWhenNoDocumentExists() {
        // given
        when(repository.findById(any())).thenReturn(Optional.empty());

        // when
        final Optional<FilingHistoryDocument> actualDocument = service.findExistingFilingHistory(TRANSACTION_ID);

        // then
        assertTrue(actualDocument.isEmpty());
        verify(repository).findById(TRANSACTION_ID);
    }

    @Test
    void insertFilingHistorySavesDocumentAndCallsKafkaApiThenReturnsUpsertSuccessful() {
        // given
        when(resourceChangedApiClient.invokeChsKafkaApi(any())).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);

        // when
        final ServiceResult actualResult = service.insertFilingHistory(document);

        // then
        assertEquals(ServiceResult.UPSERT_SUCCESSFUL, actualResult);
        verify(repository).save(document);
        verify(resourceChangedApiClient).invokeChsKafkaApi(any());
    }

    @Test
    void updateFilingHistorySavesDocumentAndCallsKafkaApiThenReturnsUpsertSuccessful() {
        FilingHistoryDocument existingDocument = Mockito.mock(FilingHistoryDocument.class);

        // given
        when(resourceChangedApiClient.invokeChsKafkaApi(any())).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);

        // when
        final ServiceResult actualResult = service.updateFilingHistory(document, existingDocument);

        // then
        assertEquals(ServiceResult.UPSERT_SUCCESSFUL, actualResult);
        verify(repository).save(document);
        verify(resourceChangedApiClient).invokeChsKafkaApi(any());
    }
}
