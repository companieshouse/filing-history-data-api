package uk.gov.companieshouse.filinghistory.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.ServiceResult;
import uk.gov.companieshouse.filinghistory.api.repository.Repository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilingHistoryServiceTest {

    private static final String TRANSACTION_ID = "transactionId";

    @InjectMocks
    private FilingHistoryService service;

    @Mock
    private Repository repository;
    @Mock
    private FilingHistoryDocument document;

    @Test
    void findExistingFilingHistoryDocumentShouldReturnDocument() {
        // given
        when(repository.findById(any())).thenReturn(Optional.of(document));

        // when
        final Optional<FilingHistoryDocument> actualDocument = service.findExistingFilingHistory(TRANSACTION_ID);

        // then
        assertTrue(actualDocument.isPresent());
        verify(repository).findById(TRANSACTION_ID);
    }

    @Test
    void findExistingFilingHistoryDocumentShouldReturnEmptyWhenNoDocumentExists() {
        // given
        when(repository.findById(any())).thenReturn(Optional.empty());

        // when
        final Optional<FilingHistoryDocument> actualDocument = service.findExistingFilingHistory(TRANSACTION_ID);

        // then
        assertTrue(actualDocument.isEmpty());
        verify(repository).findById(TRANSACTION_ID);
    }

    @Test
    void saveFilingHistoryShouldSaveDocumentAndReturnUpsertSuccessful() {
        // given

        // when
        final ServiceResult actualResult = service.saveFilingHistory(document);

        // then
        assertEquals(ServiceResult.UPSERT_SUCCESSFUL, actualResult);
        verify(repository).save(document);
    }
}
