package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import uk.gov.companieshouse.filinghistory.api.exception.BadRequestException;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.mapper.delete.DeleteMapperDelegator;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDeleteRequest;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@ExtendWith(MockitoExtension.class)
class FilingHistoryDeleteProcessorTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String ENTITY_ID = "entity_id";
    private static final String DELTA_AT = "20151025185208001000";
    private static final String STALE_DELTA_AT = "20141025185208001000";

    @InjectMocks
    private FilingHistoryDeleteProcessor filingHistoryDeleteProcessor;
    @Mock
    private FilingHistoryService filingHistoryService;
    @Mock
    private DeleteMapperDelegator deleteMapperDelegator;

    @Mock
    private FilingHistoryDocument existingDocument;
    @Mock
    private FilingHistoryDocument updatedDocument;

    @Test
    void shouldCallDeleteWhenDocumentFoundAndDelegatorReturnsEmpty() {
        // given
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.of(existingDocument));
        when(deleteMapperDelegator.delegateDelete(any(), any(), any())).thenReturn(Optional.empty());

        // when
        filingHistoryDeleteProcessor.processFilingHistoryDelete(
                new FilingHistoryDeleteRequest(COMPANY_NUMBER, TRANSACTION_ID, ENTITY_ID, DELTA_AT));

        // then
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verify(deleteMapperDelegator).delegateDelete(ENTITY_ID, existingDocument, DELTA_AT);
        verify(filingHistoryService).deleteExistingFilingHistory(existingDocument, COMPANY_NUMBER, TRANSACTION_ID);
    }

    @Test
    void shouldCallUpdateWhenDocumentFoundAndDelegatorReturnsUpdatedDocument() {
        // given
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.of(existingDocument));
        when(deleteMapperDelegator.delegateDelete(any(), any(), any())).thenReturn(Optional.of(updatedDocument));

        // when
        filingHistoryDeleteProcessor.processFilingHistoryDelete(
                new FilingHistoryDeleteRequest(COMPANY_NUMBER, TRANSACTION_ID, ENTITY_ID, DELTA_AT));

        // then
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verify(deleteMapperDelegator).delegateDelete(ENTITY_ID, existingDocument, DELTA_AT);
        verify(filingHistoryService).updateFilingHistory(updatedDocument, COMPANY_NUMBER, TRANSACTION_ID);
    }

    @Test
    void shouldCallResourceChangedWhenChildNotFoundButParentExists() {
        // given
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.of(existingDocument));
        when(deleteMapperDelegator.delegateDelete(any(), any(), any())).thenReturn(Optional.of(existingDocument));

        // when
        filingHistoryDeleteProcessor.processFilingHistoryDelete(
                new FilingHistoryDeleteRequest(COMPANY_NUMBER, TRANSACTION_ID, ENTITY_ID, DELTA_AT));

        // then
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verify(deleteMapperDelegator).delegateDelete(ENTITY_ID, existingDocument, DELTA_AT);
        verify(filingHistoryService).callResourceChangedAbsentChild(COMPANY_NUMBER, TRANSACTION_ID);
    }

    @Test
    void shouldCallResourceChangedWhenParentNorChildExists() {
        // given
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.empty());

        // when
        filingHistoryDeleteProcessor.processFilingHistoryDelete(
                new FilingHistoryDeleteRequest(COMPANY_NUMBER, TRANSACTION_ID, ENTITY_ID, DELTA_AT));

        // then
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verifyNoInteractions(deleteMapperDelegator);
        verify(filingHistoryService).callResourceChangedAbsentParent(COMPANY_NUMBER, TRANSACTION_ID);
    }

    @Test
    void shouldThrowConflictExceptionWhenDeltaAtStale() {
        // given
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.of(existingDocument));
        when(deleteMapperDelegator.delegateDelete(any(), any(), any())).thenReturn(Optional.empty());
        when(existingDocument.getDeltaAt()).thenReturn(DELTA_AT);

        // when
        Executable executable = () -> filingHistoryDeleteProcessor.processFilingHistoryDelete(
                new FilingHistoryDeleteRequest(COMPANY_NUMBER, TRANSACTION_ID, ENTITY_ID, STALE_DELTA_AT));

        // then
        assertThrows(ConflictException.class, executable);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verify(deleteMapperDelegator).delegateDelete(ENTITY_ID, existingDocument, STALE_DELTA_AT);
        verifyNoMoreInteractions(filingHistoryService);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenDeltaAtIsMissing() {
        // given
        // when
        Executable executable = () -> filingHistoryDeleteProcessor.processFilingHistoryDelete(
                new FilingHistoryDeleteRequest(COMPANY_NUMBER, TRANSACTION_ID, ENTITY_ID, null));

        // then
        BadRequestException exception = assertThrows(BadRequestException.class, executable);
        assertEquals("deltaAt is null or empty", exception.getMessage());
        verifyNoInteractions(filingHistoryService);
        verifyNoInteractions(deleteMapperDelegator);
    }
}
