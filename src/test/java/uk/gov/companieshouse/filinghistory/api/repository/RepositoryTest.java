package uk.gov.companieshouse.filinghistory.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryListAggregate;

@ExtendWith(MockitoExtension.class)
class RepositoryTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";
    private static final int START_INDEX = 0;
    private static final int ITEMS_PER_PAGE = 25;
    private static final String CATEGORY = "officers";

    @InjectMocks
    private Repository repository;
    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private FilingHistoryDocument document;
    @Mock
    private AggregationResults<FilingHistoryListAggregate> aggregationResults;
    @Mock
    private FilingHistoryListAggregate listAggregate;

    @Test
    void shouldCallMongoTemplateWithCompanyNumberCriteriaOnly() {
        // given
        when(mongoTemplate.aggregate(any(), eq(FilingHistoryDocument.class),
                eq(FilingHistoryListAggregate.class))).thenReturn(aggregationResults);
        when(aggregationResults.getUniqueMappedResult()).thenReturn(listAggregate);

        // when
        FilingHistoryListAggregate actual = repository.findCompanyFilingHistory(COMPANY_NUMBER, START_INDEX,
                ITEMS_PER_PAGE, List.of());

        // then
        assertEquals(listAggregate, actual);
    }

    @Test
    void shouldCallMongoTemplateWithCompanyNumberAndCategoryCriteria() {
        // given
        when(mongoTemplate.aggregate(any(), eq(FilingHistoryDocument.class),
                eq(FilingHistoryListAggregate.class))).thenReturn(aggregationResults);
        when(aggregationResults.getUniqueMappedResult()).thenReturn(listAggregate);

        // when
        FilingHistoryListAggregate actual = repository.findCompanyFilingHistory(COMPANY_NUMBER, START_INDEX,
                ITEMS_PER_PAGE, List.of(CATEGORY));

        // then
        assertEquals(listAggregate, actual);
    }

    @Test
    void shouldCatchDataAccessExceptionWhenFindingDocumentByIdAndThrowServiceUnavailableException() {
        // given
        when(mongoTemplate.findOne(any(), eq(FilingHistoryDocument.class))).thenThrow(new DataAccessException("...") {
        });
        Criteria criteria = Criteria.where("_id").is(TRANSACTION_ID);
        criteria.and("company_number").is(COMPANY_NUMBER);
        Query query = new Query(criteria);

        // when
        Executable executable = () -> repository.findByIdAndCompanyNumber(TRANSACTION_ID, COMPANY_NUMBER);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(mongoTemplate).findOne(query, FilingHistoryDocument.class);
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
        when(mongoTemplate.findOne(any(), eq(FilingHistoryDocument.class))).thenThrow(new DataAccessException("...") {
        });
        Criteria criteria = Criteria.where("_id").is(TRANSACTION_ID);
        Query query = new Query(criteria);

        // when
        Executable executable = () -> repository.findById(TRANSACTION_ID);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(mongoTemplate).findOne(query, FilingHistoryDocument.class);
    }
}
