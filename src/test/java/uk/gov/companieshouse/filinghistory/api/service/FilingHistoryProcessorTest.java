package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.mapper.TopLevelMapper;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.ServiceResult;

@ExtendWith(MockitoExtension.class)
class FilingHistoryProcessorTest {

    private static final String TRANSACTION_ID = "transactionId";

    @InjectMocks
    private FilingHistoryProcessor filingHistoryProcessor;

    @Mock
    private FilingHistoryService filingHistoryService;
    @Mock
    private TopLevelMapper topLevelMapper;

    @Mock
    private InternalFilingHistoryApi request;
    @Mock
    private FilingHistoryDocument documentToUpsert;

    @Mock
    private FilingHistoryDocument existingDocument;


    @Test
    void shouldSuccessfullyCallSaveWhenInsert() {
        // given
        when(filingHistoryService.findExistingFilingHistory(any())).thenReturn(Optional.empty());
        when(filingHistoryService.saveFilingHistory(any())).thenReturn(ServiceResult.UPSERT_SUCCESSFUL);
        when(topLevelMapper.mapNewFilingHistory(anyString(), any())).thenReturn(documentToUpsert);

        // when
        final ServiceResult actual = filingHistoryProcessor.processFilingHistory(TRANSACTION_ID, request);

        // then
        assertEquals(ServiceResult.UPSERT_SUCCESSFUL, actual);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID);
        verify(topLevelMapper).mapNewFilingHistory(TRANSACTION_ID, request);
        verifyNoMoreInteractions(topLevelMapper);
        verify(filingHistoryService).saveFilingHistory(documentToUpsert);
    }

    @Test
    void shouldSuccessfullyCallSaveWhenUpdate() {
        // given
        when(filingHistoryService.findExistingFilingHistory(any())).thenReturn(Optional.of(existingDocument));
        when(filingHistoryService.saveFilingHistory(any())).thenReturn(ServiceResult.UPSERT_SUCCESSFUL);
        when(topLevelMapper.mapFilingHistoryUnlessStale(any(), any(FilingHistoryDocument.class))).thenReturn(
                Optional.of(documentToUpsert));

        // when
        final ServiceResult actual = filingHistoryProcessor.processFilingHistory(TRANSACTION_ID, request);

        // then
        assertEquals(ServiceResult.UPSERT_SUCCESSFUL, actual);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID);
        verify(topLevelMapper).mapFilingHistoryUnlessStale(request, existingDocument);
        verifyNoMoreInteractions(topLevelMapper);
        verify(filingHistoryService).saveFilingHistory(documentToUpsert);
    }

    @Test
    void shouldSuccessfullyCallSaveWhenUpdateButStaleDeltaAt() {
        // given
        when(filingHistoryService.findExistingFilingHistory(any())).thenReturn(Optional.of(existingDocument));

        // when
        final ServiceResult actual = filingHistoryProcessor.processFilingHistory(TRANSACTION_ID, request);

        // then
        assertEquals(ServiceResult.STALE_DELTA, actual);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID);
        verify(topLevelMapper).mapFilingHistoryUnlessStale(request, existingDocument);
        verifyNoMoreInteractions(topLevelMapper);
        verifyNoMoreInteractions(filingHistoryService);
    }
}