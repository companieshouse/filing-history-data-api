package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.filinghistory.api.serdes.FilingHistoryDocumentCopier;

@ExtendWith(MockitoExtension.class)
class FilingHistoryDeleteProcessorTest {


    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";

    @InjectMocks
    private FilingHistoryDeleteProcessor filingHistoryDeleteProcessor;

    @Mock
    private FilingHistoryService filingHistoryService;
    @Mock
    private FilingHistoryDocumentCopier filingHistoryDocumentCopier;

    @Test
    void shouldSuccessfullyCallDeleteWhenRequestReceived() {
        // given
//        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.empty());

        // when
        filingHistoryDeleteProcessor.processFilingHistoryDelete(TRANSACTION_ID);

        // then
        verify(filingHistoryService).deleteExistingFilingHistory(existingDoc);
//        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
    }

//    @Test
//    void shouldSuccessfullyCallSaveWhenUpdateButStaleDeltaAt() {
//        // given
//        when(filingHistoryPutRequestValidator.isValid(any())).thenReturn(true);
//        when(request.getInternalData()).thenReturn(internalData);
//        when(internalData.getTransactionKind()).thenReturn(TransactionKindEnum.TOP_LEVEL);
//        when(mapperFactory.getTransactionMapper(any())).thenReturn(topLevelMapper);
//        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.of(existingDocument));
//        when(topLevelMapper.mapFilingHistoryUnlessStale(any(), any())).thenThrow(ConflictException.class);
//
//        // when
//        Executable executable = () -> filingHistoryDeleteProcessor.processFilingHistory(TRANSACTION_ID, COMPANY_NUMBER, request);
//
//        // then
//        assertThrows(ConflictException.class, executable);
//        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
//        verify(filingHistoryDocumentCopier).deepCopy(existingDocument);
//        verify(topLevelMapper).mapFilingHistoryUnlessStale(request, existingDocument);
//        verifyNoMoreInteractions(topLevelMapper);
//        verifyNoMoreInteractions(filingHistoryService);
//    }
//
//    @Test
//    void shouldThrowServiceUnavailableWhenFindingDocumentInDB() {
//        // given
//        when(filingHistoryPutRequestValidator.isValid(any())).thenReturn(true);
//        when(request.getInternalData()).thenReturn(internalData);
//        when(internalData.getTransactionKind()).thenReturn(TransactionKindEnum.TOP_LEVEL);
//        when(mapperFactory.getTransactionMapper(any())).thenReturn(topLevelMapper);
//        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenThrow(
//                ServiceUnavailableException.class);
//
//        // when
//        Executable executable = () -> filingHistoryDeleteProcessor.processFilingHistory(TRANSACTION_ID, COMPANY_NUMBER, request);
//
//        // then
//        assertThrows(ServiceUnavailableException.class, executable);
//        verify(mapperFactory).getTransactionMapper(TransactionKindEnum.TOP_LEVEL);
//        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
//        verifyNoInteractions(filingHistoryDocumentCopier);
//        verifyNoInteractions(topLevelMapper);
//        verifyNoMoreInteractions(filingHistoryService);
//    }
//

}
