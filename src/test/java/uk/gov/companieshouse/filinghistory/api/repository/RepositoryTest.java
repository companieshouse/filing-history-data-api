package uk.gov.companieshouse.filinghistory.api.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@ExtendWith(MockitoExtension.class)
class RepositoryTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";

    @InjectMocks
    private Repository repository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private FilingHistoryDocument document;

    @Test
    void shouldCatchDataAccessExceptionWhenFindingDocumentByIdAndThrowServiceUnavailableException() {
        // given
        when(mongoTemplate.findOne(any(), eq(FilingHistoryDocument.class))).thenThrow(new DataAccessException("...") {
        });

        // when
        Executable executable = () -> repository.findByIdAndCompanyNumber(TRANSACTION_ID, COMPANY_NUMBER);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
    }

    @Test
    void shouldCatchDataAccessExceptionWhenSavingDocumentAndThrowServiceUnavailableException() {
        // given
        when(mongoTemplate.save(document)).thenThrow(new DataAccessException("...") {
        });

        // when
        Executable executable = () -> repository.save(document);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
    }

    @Test
    void shouldCallSaveWithFilingHistoryDocument() {
        // given

        // when
        repository.save(new FilingHistoryDocument());

        // then
        verify(mongoTemplate).save(new FilingHistoryDocument());
    }

    @Test
    void shouldCatchDataAccessExceptionAndThrowServiceUnavailableWhenDocumentIsNotNull() {
        // given
        when(mongoTemplate.save(any())).thenThrow(new DataAccessException("...") {
        });

        // when
        Executable executable = () -> repository.save(new FilingHistoryDocument());

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(mongoTemplate).save(new FilingHistoryDocument());
        verifyNoMoreInteractions(mongoTemplate);
    }

    @Test
    void shouldCatchDataAccessExceptionAndThrowServiceUnavailableWhenDeleteById() {
        // given
        when(mongoTemplate.remove(any(), eq(FilingHistoryDocument.class))).thenThrow(new DataAccessException("...") {
        });

        // when
        Executable executable = () -> repository.deleteById(TRANSACTION_ID);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verifyNoMoreInteractions(mongoTemplate);
    }

    @Test
    void shouldCatchDataAccessExceptionAndThrowServiceUnavailableWhenFindById() {
        // given
        when(mongoTemplate.findById(any(), eq(FilingHistoryDocument.class))).thenThrow(new DataAccessException("...") {
        });

        // when
        Executable executable = () -> repository.findById(TRANSACTION_ID);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
    }
}
