package uk.gov.companieshouse.filinghistory.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.exception.NotFoundException;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.service.FilingHistoryGetResponseProcessor;
import uk.gov.companieshouse.filinghistory.api.service.FilingHistoryUpsertProcessor;

@ExtendWith(MockitoExtension.class)
class FilingHistoryControllerTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";

    @InjectMocks
    private FilingHistoryController controller;

    @Mock
    private FilingHistoryUpsertProcessor upsertProcessor;

    @Mock
    private FilingHistoryGetResponseProcessor getResponseProcessor;

    @Mock
    private InternalFilingHistoryApi requestBody;

    @Mock
    private ExternalData responseBody;

    @Test
    void shouldReturn200OKWhenPutRequest() {
        // given
        final ResponseEntity<Void> expectedResponse = ResponseEntity
                .status(HttpStatus.OK)
                .header(LOCATION, "/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID))
                .build();

        // when
        final ResponseEntity<Void> actualResponse =
                controller.upsertFilingHistoryTransaction(COMPANY_NUMBER, TRANSACTION_ID, requestBody);

        // then
        assertEquals(expectedResponse, actualResponse);
        verify(upsertProcessor).processFilingHistory(TRANSACTION_ID, COMPANY_NUMBER, requestBody);
    }

    @Test
    void shouldReturn409ConflictWhenPutRequestWithStaleDelta() {
        // given
        doThrow(ConflictException.class)
                .when(upsertProcessor).processFilingHistory(anyString(), anyString(), any());

        // when
        Executable executable = () ->
                controller.upsertFilingHistoryTransaction(COMPANY_NUMBER, TRANSACTION_ID, requestBody);

        // then
        assertThrows(ConflictException.class, executable);
        verify(upsertProcessor).processFilingHistory(TRANSACTION_ID, COMPANY_NUMBER, requestBody);
    }

    @Test
    void shouldReturn503ErrorCodeWhenResultIsServiceUnavailable() {
        // given
        doThrow(ServiceUnavailableException.class)
                .when(upsertProcessor).processFilingHistory(anyString(), anyString(), any());

        // when
        Executable executable = () ->
                controller.upsertFilingHistoryTransaction(COMPANY_NUMBER, TRANSACTION_ID, requestBody);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(upsertProcessor).processFilingHistory(TRANSACTION_ID, COMPANY_NUMBER, requestBody);
    }

    @Test
    void shouldReturn200OKWhenGetSingleTransaction() {
        // given
        final ResponseEntity<ExternalData> expectedResponse = ResponseEntity
                .status(HttpStatus.OK)
                .body(responseBody);

        when(getResponseProcessor.processGetSingleFilingHistory(any(), any())).thenReturn(responseBody);

        // when
        final ResponseEntity<ExternalData> actualResponse =
                controller.getSingleFilingHistory(COMPANY_NUMBER, TRANSACTION_ID);

        // then
        assertEquals(expectedResponse, actualResponse);
        verify(getResponseProcessor).processGetSingleFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
    }

    @Test
    void shouldReturn404NotFoundWhenGetSingleTransaction() {
        // given
        when(getResponseProcessor.processGetSingleFilingHistory(any(), any())).thenThrow(NotFoundException.class);

        // then
        Executable executable = () -> controller.getSingleFilingHistory(COMPANY_NUMBER, TRANSACTION_ID);

        // when
        assertThrows(NotFoundException.class, executable);
    }
}
