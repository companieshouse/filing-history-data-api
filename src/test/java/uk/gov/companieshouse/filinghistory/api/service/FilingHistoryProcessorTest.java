package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.NotFoundException;
import uk.gov.companieshouse.filinghistory.api.mapper.AbstractTransactionMapperFactory;
import uk.gov.companieshouse.filinghistory.api.mapper.response.ItemResponseMapper;
import uk.gov.companieshouse.filinghistory.api.mapper.TopLevelTransactionMapper;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@ExtendWith(MockitoExtension.class)
class FilingHistoryProcessorTest {

    private static final String TRANSACTION_ID = "transactionId";

    @InjectMocks
    private FilingHistoryProcessor filingHistoryProcessor;

    @Mock
    private FilingHistoryService filingHistoryService;
    @Mock
    private AbstractTransactionMapperFactory mapperFactory;
    @Mock
    private TopLevelTransactionMapper topLevelMapper;
    @Mock
    private ItemResponseMapper itemResponseMapper;

    @Mock
    private InternalFilingHistoryApi request;
    @Mock
    private ExternalData externalData;
    @Mock
    private InternalData internalData;
    @Mock
    private FilingHistoryDocument documentToUpsert;

    @Mock
    private FilingHistoryDocument existingDocument;


    @Test
    void shouldSuccessfullyCallSaveWhenInsert() {
        // given
        when(request.getInternalData()).thenReturn(internalData);
        when(internalData.getTransactionKind()).thenReturn(TransactionKindEnum.TOP_LEVEL);
        when(mapperFactory.getTransactionMapper(any())).thenReturn(topLevelMapper);
        when(filingHistoryService.findExistingFilingHistory(any())).thenReturn(Optional.empty());
        when(topLevelMapper.mapNewFilingHistory(anyString(), any())).thenReturn(documentToUpsert);
        when(filingHistoryService.insertFilingHistory(any())).thenReturn(ServiceResult.UPSERT_SUCCESSFUL);

        // when
        final ServiceResult actual = filingHistoryProcessor.processFilingHistory(TRANSACTION_ID, request);

        // then
        assertEquals(ServiceResult.UPSERT_SUCCESSFUL, actual);
        verify(mapperFactory).getTransactionMapper(TransactionKindEnum.TOP_LEVEL);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID);
        verify(topLevelMapper).mapNewFilingHistory(TRANSACTION_ID, request);
        verifyNoMoreInteractions(topLevelMapper);
        verify(filingHistoryService).insertFilingHistory(documentToUpsert);
    }

    @Test
    void shouldSuccessfullyCallSaveWhenUpdate() {
        // given
        when(request.getInternalData()).thenReturn(internalData);
        when(internalData.getTransactionKind()).thenReturn(TransactionKindEnum.TOP_LEVEL);
        when(mapperFactory.getTransactionMapper(any())).thenReturn(topLevelMapper);
        when(filingHistoryService.findExistingFilingHistory(any())).thenReturn(Optional.of(existingDocument));
        when(filingHistoryService.insertFilingHistory(any())).thenReturn(ServiceResult.UPSERT_SUCCESSFUL);
        when(topLevelMapper.mapFilingHistoryUnlessStale(any(), any(FilingHistoryDocument.class))).thenReturn(
                Optional.of(documentToUpsert));

        // when
        final ServiceResult actual = filingHistoryProcessor.processFilingHistory(TRANSACTION_ID, request);

        // then
        assertEquals(ServiceResult.UPSERT_SUCCESSFUL, actual);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID);
        verify(topLevelMapper).mapFilingHistoryUnlessStale(request, existingDocument);
        verifyNoMoreInteractions(topLevelMapper);
        verify(filingHistoryService).updateFilingHistory(documentToUpsert, existingDocument);
    }

    @Test
    void shouldSuccessfullyCallSaveWhenUpdateButStaleDeltaAt() {
        // given
        when(request.getInternalData()).thenReturn(internalData);
        when(internalData.getTransactionKind()).thenReturn(TransactionKindEnum.TOP_LEVEL);
        when(mapperFactory.getTransactionMapper(any())).thenReturn(topLevelMapper);
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

    @Test
    void shouldSuccessfullyGetSingleFilingHistoryDocumentAndReturnExternalData() {
        // given
        final ExternalData expected = externalData;
        when(itemResponseMapper.mapFilingHistory(any())).thenReturn(externalData);
        when(filingHistoryService.findExistingFilingHistory(any())).thenReturn(Optional.of(existingDocument));

        // when
        final ExternalData actual = filingHistoryProcessor.processGetSingleFilingHistory("12345678", TRANSACTION_ID);

        // then
        assertEquals(expected, actual);
        verify(itemResponseMapper).mapFilingHistory(existingDocument);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoDocumentInDB() {
        // given
        when(filingHistoryService.findExistingFilingHistory(any())).thenReturn(Optional.empty());

        // when
        Executable executable = () -> filingHistoryProcessor.processGetSingleFilingHistory("12345678", TRANSACTION_ID);

        // then
        assertThrows(NotFoundException.class, executable);
        verifyNoInteractions(itemResponseMapper);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID);
    }
}