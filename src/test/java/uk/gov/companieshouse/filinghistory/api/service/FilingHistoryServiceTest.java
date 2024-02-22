package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.filinghistory.api.client.ResourceChangedApiClient;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.repository.Repository;

@ExtendWith(MockitoExtension.class)
class FilingHistoryServiceTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";

    @InjectMocks
    private FilingHistoryService service;

    @Mock
    private ResourceChangedApiClient resourceChangedApiClient;
    @Mock
    private Repository repository;
    @Mock
    private FilingHistoryDocument document;
    @Mock
    private FilingHistoryDocument existingDocument;
    @Mock
    private ApiResponse<Void> response;

    @Test
    void findExistingFilingHistoryDocumentShouldReturnDocument() {
        // given
        when(repository.findByIdAndCompanyNumber(any(), any())).thenReturn(Optional.of(document));

        // when
        final Optional<FilingHistoryDocument> actualDocument = service.findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);

        // then
        assertTrue(actualDocument.isPresent());
        verify(repository).findByIdAndCompanyNumber(TRANSACTION_ID, COMPANY_NUMBER);
    }

    @Test
    void findExistingFilingHistoryDocumentShouldReturnEmptyWhenNoDocumentExists() {
        // given
        when(repository.findByIdAndCompanyNumber(any(), any())).thenReturn(Optional.empty());

        // when
        final Optional<FilingHistoryDocument> actualDocument = service.findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);

        // then
        assertTrue(actualDocument.isEmpty());
        verify(repository).findByIdAndCompanyNumber(TRANSACTION_ID, COMPANY_NUMBER);
    }

    @Test
    void insertFilingHistorySavesDocumentAndCallsKafkaApiThenReturnsUpsertSuccessful() throws Exception {
        // given
        when(resourceChangedApiClient.callResourceChanged(any())).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);

        // when
        service.insertFilingHistory(document);

        // then
        verify(repository).save(document);
        verify(resourceChangedApiClient).callResourceChanged(any());
    }

    @Test
    void updateFilingHistorySavesDocumentAndCallsKafkaApiThenReturnsUpsertSuccessful() {
        // given
        when(resourceChangedApiClient.callResourceChanged(any())).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);

        // when
        service.updateFilingHistory(document, existingDocument);

        // then
        verify(repository).save(document);
        verify(resourceChangedApiClient).callResourceChanged(any());
    }

    @Test
    void updateFilingHistorySavesDocumentButResourceChangedCallReturnsNon200() throws Exception {
        // given
        when(resourceChangedApiClient.callResourceChanged(any())).thenReturn(response);
        when(response.getStatusCode()).thenReturn(503);
        when(document.getTransactionId()).thenReturn(TRANSACTION_ID);

        // when
        Executable executable = () -> service.updateFilingHistory(document, existingDocument);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(repository).save(document);
        verify(resourceChangedApiClient).callResourceChanged(any());
        verify(repository).save(existingDocument);
    }

    @Test
    void findExistingFilingHistoryDocumentShouldThrowServiceUnavailableExceptionWhenCatchingServiceUnavailableException() {
        // given
        when(repository.findByIdAndCompanyNumber(any(), any())).thenThrow(ServiceUnavailableException.class);

        // when
        Executable executable = () -> service.findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(repository).findByIdAndCompanyNumber(TRANSACTION_ID, COMPANY_NUMBER);
    }

    @Test
    void upsertExistingFilingHistoryDocumentShouldReturnServiceUnavailableResultWhenCatchingServiceUnavailableException() {
        // given
        doThrow(ServiceUnavailableException.class)
                .when(repository).save(any());

        // when
        Executable executable = () -> service.insertFilingHistory(document);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(repository).save(document);
        verifyNoInteractions(resourceChangedApiClient);
    }
}
