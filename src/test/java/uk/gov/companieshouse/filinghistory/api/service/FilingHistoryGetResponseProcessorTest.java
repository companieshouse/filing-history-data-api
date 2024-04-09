package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryList;
import uk.gov.companieshouse.filinghistory.api.exception.NotFoundException;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.mapper.get.ItemGetResponseMapper;
import uk.gov.companieshouse.filinghistory.api.mapper.get.ListGetResponseMapper;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryListRequestParams;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryListAggregate;

@ExtendWith(MockitoExtension.class)
class FilingHistoryGetResponseProcessorTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String STATUS = "filing-history-not-available-limited-partnership-before-1988";
    private static final String NOT_AVAILABLE_STATUS = "filing-history-not-available";
    private static final int DEFAULT_ITEMS_PER_PAGE = 25;
    private static final int MAX_ITEMS_PER_PAGE = 100;
    private static final int START_INDEX = 0;
    private static final List<String> CATEGORIES = List.of();

    @InjectMocks
    private FilingHistoryGetResponseProcessor processor;

    @Mock
    private FilingHistoryService filingHistoryService;
    @Mock
    private ItemGetResponseMapper itemGetResponseMapper;
    @Mock
    private StatusService statusService;
    @Mock
    private ListGetResponseMapper listGetResponseMapper;

    @Mock
    private ExternalData externalData;
    @Mock
    private FilingHistoryDocument existingDocument;
    @Mock
    private FilingHistoryList filingHistoryList;
    @Mock
    private FilingHistoryListAggregate listAggregate;

    @Test
    void shouldSuccessfullyGetSingleFilingHistoryDocumentAndReturnExternalData() {
        // given
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.of(existingDocument));
        when(itemGetResponseMapper.mapFilingHistoryItem(any())).thenReturn(externalData);

        // when
        final ExternalData actual = processor.processGetSingleFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);

        // then
        assertEquals(externalData, actual);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verify(itemGetResponseMapper).mapFilingHistoryItem(existingDocument);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoDocumentInDB() {
        // given
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.empty());

        // when
        Executable executable = () -> processor.processGetSingleFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);

        // then
        assertThrows(NotFoundException.class, executable);
        verifyNoInteractions(itemGetResponseMapper);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
    }

    @Test
    void processGetSingleFilingHistoryShouldThrowServiceUnavailableException() {
        // given
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenThrow(ServiceUnavailableException.class);;

        // when
        Executable executable = () -> processor.processGetSingleFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verifyNoInteractions(itemGetResponseMapper);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
    }

    @Test
    void shouldSuccessfullyReturnCompanyFilingHistoryList() {
        // given
        when(statusService.processStatus(any())).thenReturn(STATUS);
        when(filingHistoryService.findCompanyFilingHistoryList(any(), anyInt(), anyInt(), any()))
                .thenReturn(Optional.of(listAggregate));
        when(listGetResponseMapper.mapFilingHistoryList(anyInt(), anyInt(), any(), any())).thenReturn(
                filingHistoryList);

        FilingHistoryListRequestParams requestParams = FilingHistoryListRequestParams.builder()
                .companyNumber(COMPANY_NUMBER)
                .startIndex(START_INDEX)
                .itemsPerPage(DEFAULT_ITEMS_PER_PAGE)
                .categories(CATEGORIES)
                .build();

        // when
        final FilingHistoryList actual = processor.processGetCompanyFilingHistoryList(requestParams);

        // then
        assertEquals(filingHistoryList, actual);
        verify(statusService).processStatus(COMPANY_NUMBER);
        verify(filingHistoryService).findCompanyFilingHistoryList(COMPANY_NUMBER, START_INDEX, DEFAULT_ITEMS_PER_PAGE,
                CATEGORIES);
        verify(listGetResponseMapper).mapFilingHistoryList(START_INDEX, DEFAULT_ITEMS_PER_PAGE, STATUS, listAggregate);
    }

    @Test
    void shouldSuccessfullyReturnCompanyFilingHistoryListWhenItemsPerPageHigherThanMax() {
        // given

        int highItemsPerPage = 150;
        when(statusService.processStatus(any())).thenReturn(STATUS);
        when(filingHistoryService.findCompanyFilingHistoryList(any(), anyInt(), anyInt(), any()))
                .thenReturn(Optional.of(listAggregate));
        when(listGetResponseMapper.mapFilingHistoryList(anyInt(), anyInt(), any(), any())).thenReturn(
                filingHistoryList);

        FilingHistoryListRequestParams requestParams = FilingHistoryListRequestParams.builder()
                .companyNumber(COMPANY_NUMBER)
                .startIndex(START_INDEX)
                .itemsPerPage(highItemsPerPage)
                .categories(CATEGORIES)
                .build();

        // when
        final FilingHistoryList actual = processor.processGetCompanyFilingHistoryList(requestParams);

        // then
        assertEquals(filingHistoryList, actual);
        verify(statusService).processStatus(COMPANY_NUMBER);
        verify(filingHistoryService).findCompanyFilingHistoryList(COMPANY_NUMBER, START_INDEX, MAX_ITEMS_PER_PAGE,
                CATEGORIES);
        verify(listGetResponseMapper).mapFilingHistoryList(START_INDEX, MAX_ITEMS_PER_PAGE, STATUS, listAggregate);
    }

    @Test
    void shouldSuccessfullyReturnBaseCompanyFilingHistoryListWhenStatusNotAvailable() {
        // given
        when(statusService.processStatus(any())).thenReturn(NOT_AVAILABLE_STATUS);
        when(listGetResponseMapper.mapBaseFilingHistoryList(anyInt(), anyInt(), any())).thenReturn(filingHistoryList);

        FilingHistoryListRequestParams requestParams = FilingHistoryListRequestParams.builder()
                .companyNumber(COMPANY_NUMBER)
                .startIndex(START_INDEX)
                .itemsPerPage(DEFAULT_ITEMS_PER_PAGE)
                .categories(CATEGORIES)
                .build();

        // when
        final FilingHistoryList actual = processor.processGetCompanyFilingHistoryList(requestParams);

        // then
        assertEquals(filingHistoryList, actual);
        verify(statusService).processStatus(COMPANY_NUMBER);
        verify(listGetResponseMapper).mapBaseFilingHistoryList(START_INDEX, DEFAULT_ITEMS_PER_PAGE,
                NOT_AVAILABLE_STATUS);
        verifyNoInteractions(filingHistoryService);
        verifyNoMoreInteractions(listGetResponseMapper);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoDocumentsInDBForFilingHistoryList() {
        // given
        when(statusService.processStatus(any())).thenReturn(STATUS);
        when(listGetResponseMapper.mapBaseFilingHistoryList(anyInt(), anyInt(), any())).thenReturn(filingHistoryList);
        when(filingHistoryService.findCompanyFilingHistoryList(any(), anyInt(), anyInt(), any()))
                .thenReturn(Optional.empty());

        FilingHistoryListRequestParams requestParams = FilingHistoryListRequestParams.builder()
                .companyNumber(COMPANY_NUMBER)
                .startIndex(START_INDEX)
                .itemsPerPage(DEFAULT_ITEMS_PER_PAGE)
                .categories(CATEGORIES)
                .build();

        // when
        final FilingHistoryList actual = processor.processGetCompanyFilingHistoryList(requestParams);

        // then
        assertEquals(filingHistoryList, actual);
        verify(statusService).processStatus(COMPANY_NUMBER);
        verify(listGetResponseMapper).mapBaseFilingHistoryList(START_INDEX, DEFAULT_ITEMS_PER_PAGE, STATUS);
        verify(filingHistoryService).findCompanyFilingHistoryList(COMPANY_NUMBER, START_INDEX, DEFAULT_ITEMS_PER_PAGE,
                CATEGORIES);
        verifyNoMoreInteractions(listGetResponseMapper);
    }

    @Test
    void processGetCompanyFilingHistoryListShouldThrowServiceUnavailableException() {
        // given
        when(statusService.processStatus(any())).thenReturn(STATUS);
        when(listGetResponseMapper.mapBaseFilingHistoryList(anyInt(), anyInt(), any())).thenReturn(filingHistoryList);
        when(filingHistoryService.findCompanyFilingHistoryList(any(), anyInt(),
                anyInt(), any())).thenThrow(ServiceUnavailableException.class);

        FilingHistoryListRequestParams requestParams = FilingHistoryListRequestParams.builder()
                .companyNumber(COMPANY_NUMBER)
                .startIndex(START_INDEX)
                .itemsPerPage(DEFAULT_ITEMS_PER_PAGE)
                .categories(CATEGORIES)
                .build();

        // when
        Executable executable = () -> processor.processGetCompanyFilingHistoryList(requestParams);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(statusService).processStatus(COMPANY_NUMBER);
        verify(listGetResponseMapper).mapBaseFilingHistoryList(START_INDEX, DEFAULT_ITEMS_PER_PAGE, STATUS);
        verify(filingHistoryService).findCompanyFilingHistoryList(COMPANY_NUMBER, START_INDEX, DEFAULT_ITEMS_PER_PAGE,
                CATEGORIES);
        verifyNoMoreInteractions(listGetResponseMapper);
    }
}
