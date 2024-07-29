package uk.gov.companieshouse.filinghistory.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
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
import uk.gov.companieshouse.filinghistory.api.exception.BadGatewayException;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeleteAggregate;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryListAggregate;
import uk.gov.companieshouse.filinghistory.api.model.mongo.UnversionedFilingHistoryDocument;

@ExtendWith(MockitoExtension.class)
class RepositoryTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String ENTITY_ID = "entityId";
    private static final String COMPANY_NUMBER = "12345678";
    private static final int START_INDEX = 0;
    private static final int ITEMS_PER_PAGE = 25;
    private static final String CATEGORY = "officers";

    @InjectMocks
    private Repository repository;
    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private AggregationResults<FilingHistoryListAggregate> aggregationResults;
    @Mock
    private FilingHistoryListAggregate listAggregate;
    @Mock
    private AggregationResults<FilingHistoryDeleteAggregate> deleteAggregationResults;
    @Mock
    private FilingHistoryDeleteAggregate deleteAggregate;

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
    void shouldCatchDataAccessExceptionWhenFindingDocumentByIdAndThrowBadGatewayException() {
        // given
        when(mongoTemplate.findOne(any(), eq(FilingHistoryDocument.class))).thenThrow(new DataAccessException("...") {
        });
        Criteria criteria = Criteria.where("_id").is(TRANSACTION_ID)
                .and("company_number").is(COMPANY_NUMBER);
        Query query = new Query(criteria);

        // when
        Executable executable = () -> repository.findByIdAndCompanyNumber(TRANSACTION_ID, COMPANY_NUMBER);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(mongoTemplate).findOne(query, FilingHistoryDocument.class);
    }

    @Test
    void shouldCallInsertWithFilingHistoryDocument() {
        // given

        // when
        repository.insert(new FilingHistoryDocument());

        // then
        verify(mongoTemplate).insert(new FilingHistoryDocument());
    }

    @Test
    void shouldCatchDataAccessExceptionAndThrowBadGatewayWhenInserting() {
        // given
        when(mongoTemplate.insert(any(FilingHistoryDocument.class))).thenThrow(new DataAccessException("...") {
        });

        // when
        Executable executable = () -> repository.insert(new FilingHistoryDocument());

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(mongoTemplate).insert(new FilingHistoryDocument());
    }

    @Test
    void shouldCallSaveWithFilingHistoryDocument() {
        // given
        FilingHistoryDocument document = new FilingHistoryDocument()
                .version(0);

        // when
        repository.update(document);

        // then
        verify(mongoTemplate).save(document);
    }

    @Test
    void shouldCallSaveWithUnversionedFilingHistoryDocumentWhenNullVersion() {
        // given

        // when
        repository.update(new FilingHistoryDocument());

        // then
        verify(mongoTemplate).save(new UnversionedFilingHistoryDocument());
    }

    @Test
    void shouldCatchDataAccessExceptionAndThrowBadGatewayWhenUpdating() {
        // given
        when(mongoTemplate.save(any(FilingHistoryDocument.class))).thenThrow(new DataAccessException("...") {
        });

        FilingHistoryDocument document = new FilingHistoryDocument()
                .version(0);

        // when
        Executable executable = () -> repository.update(document);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(mongoTemplate).save(document);
    }

    @Test
    void shouldCatchDataAccessExceptionAndThrowBadGatewayWhenDeleteById() {
        // given
        when(mongoTemplate.remove(any(), eq(FilingHistoryDocument.class))).thenThrow(new DataAccessException("...") {
        });

        // when
        Executable executable = () -> repository.deleteById(TRANSACTION_ID);

        // then
        assertThrows(BadGatewayException.class, executable);
        verifyNoMoreInteractions(mongoTemplate);
    }

    @Test
    void shouldCallMongoTemplateWithEntityIdCriteria() {
        // given
        when(mongoTemplate.aggregate(any(), eq(FilingHistoryDocument.class),
                eq(FilingHistoryDeleteAggregate.class))).thenReturn(deleteAggregationResults);
        when(deleteAggregationResults.getUniqueMappedResult()).thenReturn(deleteAggregate);

        // when
        Optional<FilingHistoryDeleteAggregate> actual = repository.findByEntityId(ENTITY_ID);

        // then
        assertTrue(actual.isPresent());
        assertEquals(deleteAggregate, actual.get());
    }

    @Test
    void shouldCatchDataAccessExceptionAndThrowBadGatewayWhenFindByEntityId() {
        // given
        when(mongoTemplate.aggregate(any(), eq(FilingHistoryDocument.class), eq(FilingHistoryDeleteAggregate.class)))
                .thenThrow(new DataAccessException("...") {
                });

        // when
        Executable executable = () -> repository.findByEntityId(ENTITY_ID);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(mongoTemplate).aggregate(any(), eq(FilingHistoryDocument.class), eq(FilingHistoryDeleteAggregate.class));
    }

    @Test
    void shouldCatchDataAccessExceptionAndThrowBadGatewayWhenFindCompanyFilingHistory() {
        // given
        when(mongoTemplate.aggregate(any(), eq(FilingHistoryDocument.class), eq(FilingHistoryListAggregate.class)))
                .thenThrow(new DataAccessException("...") {
                });

        // when
        Executable executable = () -> repository.findCompanyFilingHistory(COMPANY_NUMBER, START_INDEX,
                ITEMS_PER_PAGE, List.of(CATEGORY));

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(mongoTemplate).aggregate(any(), eq(FilingHistoryDocument.class), eq(FilingHistoryListAggregate.class));
    }
}
