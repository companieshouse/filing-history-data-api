package uk.gov.companieshouse.filinghistory.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.ServiceResult;
import uk.gov.companieshouse.filinghistory.api.service.FilingHistoryProcessor;

@ExtendWith(MockitoExtension.class)
class FilingHistoryControllerTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";

    @InjectMocks
    private FilingHistoryController controller;

    @Mock
    private FilingHistoryProcessor service;

    @Mock
    private InternalFilingHistoryApi requestBody;

    @Test
    void shouldReturn200OKWhenPutRequest() {
        // given
        final ResponseEntity<Void> expectedResponse = ResponseEntity
                .status(HttpStatus.OK)
                .header(LOCATION, "/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID))
                .build();

        when(service.processFilingHistory(any(), any())).thenReturn(ServiceResult.UPSERT_SUCCESSFUL);

        // when
        final ResponseEntity<Void> actualResponse =
                controller.upsertFilingHistoryTransaction(COMPANY_NUMBER, TRANSACTION_ID, requestBody);

        // then
        assertEquals(expectedResponse, actualResponse);
        verify(service).processFilingHistory(TRANSACTION_ID, requestBody);
    }

    @Test
    void shouldReturn409ConflictWhenPutRequestWithStaleDelta() {
        // given
        final ResponseEntity<Void> expectedResponse = ResponseEntity
                .status(HttpStatus.CONFLICT)
                .header(LOCATION, "/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID))
                .build();

        when(service.processFilingHistory(any(), any())).thenReturn(ServiceResult.STALE_DELTA);

        // when
        final ResponseEntity<Void> actualResponse =
                controller.upsertFilingHistoryTransaction(COMPANY_NUMBER, TRANSACTION_ID, requestBody);

        // then
        assertEquals(expectedResponse, actualResponse);
        verify(service).processFilingHistory(TRANSACTION_ID, requestBody);
    }
}
