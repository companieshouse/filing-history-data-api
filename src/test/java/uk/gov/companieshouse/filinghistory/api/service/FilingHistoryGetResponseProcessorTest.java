package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.filinghistory.api.exception.NotFoundException;
import uk.gov.companieshouse.filinghistory.api.mapper.get.ItemGetResponseMapper;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@ExtendWith(MockitoExtension.class)
class FilingHistoryGetResponseProcessorTest {

    private static final String TRANSACTION_ID = "transactionId";

    @InjectMocks
    private FilingHistoryGetResponseProcessor processor;

    @Mock
    private FilingHistoryService filingHistoryService;
    @Mock
    private ItemGetResponseMapper itemGetResponseMapper;
    @Mock
    private ExternalData externalData;

    @Mock
    private FilingHistoryDocument existingDocument;

    @Test
    void shouldSuccessfullyGetSingleFilingHistoryDocumentAndReturnExternalData() {
        // given
        final ExternalData expected = externalData;
        when(itemGetResponseMapper.mapFilingHistoryItem(any())).thenReturn(externalData);
        when(filingHistoryService.findExistingFilingHistory(any())).thenReturn(Optional.of(existingDocument));

        // when
        final ExternalData actual = processor.processGetSingleFilingHistory("12345678", TRANSACTION_ID);

        // then
        assertEquals(expected, actual);
        verify(itemGetResponseMapper).mapFilingHistoryItem(existingDocument);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenNoDocumentInDB() {
        // given
        when(filingHistoryService.findExistingFilingHistory(any())).thenReturn(Optional.empty());

        // when
        Executable executable = () -> processor.processGetSingleFilingHistory("12345678", TRANSACTION_ID);

        // then
        assertThrows(NotFoundException.class, executable);
        verifyNoInteractions(itemGetResponseMapper);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID);
    }
}
