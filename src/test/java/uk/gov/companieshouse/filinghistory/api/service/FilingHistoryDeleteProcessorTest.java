package uk.gov.companieshouse.filinghistory.api.service;

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
import uk.gov.companieshouse.filinghistory.api.exception.NotFoundException;
import uk.gov.companieshouse.filinghistory.api.mapper.delete.DeleteMapperDelegator;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@ExtendWith(MockitoExtension.class)
class FilingHistoryDeleteProcessorTest {


    private static final String ENTITY_ID = "transactionId";

    @InjectMocks
    private FilingHistoryDeleteProcessor filingHistoryDeleteProcessor;
    @Mock
    private FilingHistoryService filingHistoryService;
    @Mock
    private DeleteMapperDelegator mapperFactory;

    @Mock
    private FilingHistoryDocument existingDocument;
    @Mock
    private FilingHistoryDocument updatedDocument;

    @Test
    void shouldCallDeleteWhenParentDocumentRemoved() {
        // given
        when(filingHistoryService.findFilingHistoryByEntityId(any())).thenReturn(Optional.of(existingDocument));
        when(mapperFactory.delegateDelete(any(), any())).thenReturn(Optional.empty());

        // when
        filingHistoryDeleteProcessor.processFilingHistoryDelete(ENTITY_ID);

        // then
        verify(filingHistoryService).findFilingHistoryByEntityId(ENTITY_ID);
        verify(mapperFactory).delegateDelete(ENTITY_ID, existingDocument);
        verify(filingHistoryService).deleteExistingFilingHistory(existingDocument);
    }

    @Test
    void shouldCallUpdateWhenResolutionRemovedFromComposite() {
        // given
        when(filingHistoryService.findFilingHistoryByEntityId(any())).thenReturn(Optional.of(existingDocument));
        when(mapperFactory.delegateDelete(any(), any())).thenReturn(Optional.of(updatedDocument));

        // when
        filingHistoryDeleteProcessor.processFilingHistoryDelete(ENTITY_ID);

        // then
        verify(filingHistoryService).findFilingHistoryByEntityId(ENTITY_ID);
        verify(mapperFactory).delegateDelete(ENTITY_ID, existingDocument);
        verify(filingHistoryService).updateFilingHistory(updatedDocument, existingDocument);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCannotFindDocumentInDB() {
        // given
        when(filingHistoryService.findFilingHistoryByEntityId(any())).thenReturn(Optional.empty());

        // when
        Executable executable = () -> filingHistoryDeleteProcessor.processFilingHistoryDelete(ENTITY_ID);

        // then
        assertThrows(NotFoundException.class, executable);
        verify(filingHistoryService).findFilingHistoryByEntityId(ENTITY_ID);
        verifyNoInteractions(mapperFactory);
    }
}
