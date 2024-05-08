package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.filinghistory.api.exception.NotFoundException;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@ExtendWith(MockitoExtension.class)
class FilingHistoryDeleteProcessorTest {


    private static final String TRANSACTION_ID = "transactionId";

    @InjectMocks
    private FilingHistoryDeleteProcessor filingHistoryDeleteProcessor;

    @Mock
    private FilingHistoryService filingHistoryService;
    @Mock
    private FilingHistoryDocument existingDocument;

    @Test
    void shouldSuccessfullyCallDeleteWhenRequestReceived() {
        // given
        when(filingHistoryService.findFilingHistoryByEntityId(any())).thenReturn(Optional.of(existingDocument));

        // when
        filingHistoryDeleteProcessor.processFilingHistoryDelete(TRANSACTION_ID);

        // then
        verify(filingHistoryService).deleteExistingFilingHistory(existingDocument);
        verify(filingHistoryService).findFilingHistoryByEntityId(TRANSACTION_ID);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCannotFindDocumentInDB(){
        // given
        when(filingHistoryService.findFilingHistoryByEntityId(any())).thenReturn(Optional.empty());

        // when
        Executable executable = () -> filingHistoryDeleteProcessor.processFilingHistoryDelete(TRANSACTION_ID);

        // then
        assertThrows(NotFoundException.class, executable);
        verify(filingHistoryService).findFilingHistoryByEntityId(TRANSACTION_ID);
    }

    @Test
    void shouldThrowServiceUnavailableWhenMongoDBUnavailable() {
        // given
        when(filingHistoryService.findFilingHistoryByEntityId(any())).thenThrow(
                ServiceUnavailableException.class);

        // when
        Executable executable = () -> filingHistoryDeleteProcessor.processFilingHistoryDelete(TRANSACTION_ID);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(filingHistoryService).findFilingHistoryByEntityId(TRANSACTION_ID);
        verifyNoMoreInteractions(filingHistoryService);
    }

}
