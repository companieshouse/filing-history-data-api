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
import uk.gov.companieshouse.api.filinghistory.FilingHistoryList;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.exception.NotFoundException;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryListRequestParams;
import uk.gov.companieshouse.filinghistory.api.service.FilingHistoryDeleteProcessor;
import uk.gov.companieshouse.filinghistory.api.service.FilingHistoryGetResponseProcessor;
import uk.gov.companieshouse.filinghistory.api.service.FilingHistoryUpsertProcessor;

@ExtendWith(MockitoExtension.class)
class FilingHistoryControllerTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String ENTITY_ID = "entity_id";
    private static final String COMPANY_NUMBER = "12345678";
    private static final int START_INDEX = 0;
    private static final int DEFAULT_ITEMS_PER_PAGE = 25;

    @InjectMocks
    private FilingHistoryController controller;
    @Mock
    private FilingHistoryUpsertProcessor upsertProcessor;
    @Mock
    private FilingHistoryGetResponseProcessor getResponseProcessor;
    @Mock
    private FilingHistoryDeleteProcessor deleteProcessor;

    @Mock
    private InternalFilingHistoryApi requestBody;
    @Mock
    private InternalData internalData;
    @Mock
    private ExternalData getSingleResponseBody;
    @Mock
    private FilingHistoryList getListResponseBody;

    @Test
    void shouldReturn200OKWhenGetCompanyFilingHistoryList() {
        // given
        final ResponseEntity<FilingHistoryList> expectedResponse = ResponseEntity
                .status(HttpStatus.OK)
                .body(getListResponseBody);

        final FilingHistoryListRequestParams params = FilingHistoryListRequestParams.builder()
                .companyNumber(COMPANY_NUMBER)
                .startIndex(START_INDEX)
                .itemsPerPage(DEFAULT_ITEMS_PER_PAGE)
                .build();

        when(getResponseProcessor.processGetCompanyFilingHistoryList(any())).thenReturn(getListResponseBody);

        // when
        final ResponseEntity<FilingHistoryList> actualResponse = controller.getCompanyFilingHistoryList(COMPANY_NUMBER,
                START_INDEX, DEFAULT_ITEMS_PER_PAGE, null);

        // then
        assertEquals(expectedResponse, actualResponse);
        verify(getResponseProcessor).processGetCompanyFilingHistoryList(params);
    }

    @Test
    void shouldReturn404NotFoundWhenGetCompanyFilingHistoryList() {
        // given
        when(getResponseProcessor.processGetCompanyFilingHistoryList(any())).thenThrow(NotFoundException.class);

        final FilingHistoryListRequestParams params = FilingHistoryListRequestParams.builder()
                .companyNumber(COMPANY_NUMBER)
                .startIndex(START_INDEX)
                .itemsPerPage(DEFAULT_ITEMS_PER_PAGE)
                .build();

        // then
        Executable executable = () ->
                controller.getCompanyFilingHistoryList(COMPANY_NUMBER, START_INDEX,
                        DEFAULT_ITEMS_PER_PAGE, null);

        // when
        assertThrows(NotFoundException.class, executable);
        verify(getResponseProcessor).processGetCompanyFilingHistoryList(params);
    }

    @Test
    void shouldReturn503ServiceUnavailableWhenGetCompanyFilingHistoryList() {
        // given
        when(getResponseProcessor.processGetCompanyFilingHistoryList(any())).thenThrow(ServiceUnavailableException.class);

        final FilingHistoryListRequestParams params = FilingHistoryListRequestParams.builder()
                .companyNumber(COMPANY_NUMBER)
                .startIndex(START_INDEX)
                .itemsPerPage(DEFAULT_ITEMS_PER_PAGE)
                .build();

        // then
        Executable executable = () ->
                controller.getCompanyFilingHistoryList(COMPANY_NUMBER, START_INDEX,
                        DEFAULT_ITEMS_PER_PAGE, null);

        // when
        assertThrows(ServiceUnavailableException.class, executable);
        verify(getResponseProcessor).processGetCompanyFilingHistoryList(params);
    }

    @Test
    void shouldReturn200OKWhenGetSingleTransaction() {
        // given
        final ResponseEntity<ExternalData> expectedResponse = ResponseEntity
                .status(HttpStatus.OK)
                .body(getSingleResponseBody);

        when(getResponseProcessor.processGetSingleFilingHistory(any(), any())).thenReturn(getSingleResponseBody);

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
        verify(getResponseProcessor).processGetSingleFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
    }

    @Test
    void shouldReturn503ServiceUnavailableWhenGetSingleTransaction() {
        // given
        when(getResponseProcessor.processGetSingleFilingHistory(any(), any()))
                .thenThrow(ServiceUnavailableException.class);

        // then
        Executable executable = () -> controller.getSingleFilingHistory(COMPANY_NUMBER, TRANSACTION_ID);

        // when
        assertThrows(ServiceUnavailableException.class, executable);
        verify(getResponseProcessor).processGetSingleFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
    }

    @Test
    void shouldReturn200OKWhenPutRequest() {
        // given
        when(requestBody.getInternalData()).thenReturn(internalData);
        when(internalData.getEntityId()).thenReturn(ENTITY_ID);

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
        when(requestBody.getInternalData()).thenReturn(internalData);
        when(internalData.getEntityId()).thenReturn(ENTITY_ID);

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
    void shouldReturn503ErrorCodeWhenPutRequestAndServiceUnavailable() {
        // given
        when(requestBody.getInternalData()).thenReturn(internalData);
        when(internalData.getEntityId()).thenReturn(ENTITY_ID);

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
    void shouldReturn200WhenDeleteSingleTransaction() {
        // given
        final ResponseEntity<Void> expectedResponse = ResponseEntity
                .status(HttpStatus.OK)
                .build();

        // when
        final ResponseEntity<Void> actualResponse = controller.deleteFilingHistoryTransaction(TRANSACTION_ID);

        // then
        assertEquals(expectedResponse, actualResponse);
        verify(deleteProcessor).processFilingHistoryDelete(TRANSACTION_ID);
    }

    @Test
    void shouldReturn404WhenDeleteAndNotFoundException() {
        // given
        doThrow(NotFoundException.class)
                .when(deleteProcessor).processFilingHistoryDelete(anyString());

        // when
        Executable executable = () -> controller.deleteFilingHistoryTransaction(TRANSACTION_ID);

        // then
        assertThrows(NotFoundException.class, executable);
        verify(deleteProcessor).processFilingHistoryDelete(TRANSACTION_ID);
    }
}
