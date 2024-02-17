package uk.gov.companieshouse.filinghistory.api.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @InjectMocks
    private Repository repository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private FilingHistoryDocument document;

    @Test
    void shouldCatchDataAccessExceptionWhenFindingDocumentByIdAndThrowServiceUnavailableException() {
        // given
        when(mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class)).thenThrow(new DataAccessException("..."){});

        // when
        Executable executable = () -> repository.findById(TRANSACTION_ID);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
    }

    @Test
    void shouldCatchDataAccessExceptionWhenSavingDocumentAndThrowServiceUnavailableException() {
        // given
        when(mongoTemplate.save(document)).thenThrow(new DataAccessException("..."){});

        // when
        Executable executable = () -> repository.save(document);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
    }
}
