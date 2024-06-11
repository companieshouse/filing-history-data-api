package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.filinghistory.api.exception.BadRequestException;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeleteAggregate;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@ExtendWith(MockitoExtension.class)
class DeleteMapperDelegatorAspectTest {

    private static final String ENTITY_ID = "entity ID";
    private static final String CHILD_ENTITY_ID = "child entity ID";
    private final DeleteMapperDelegatorAspect aspect = new DeleteMapperDelegatorAspect();

    @Mock
    private JoinPoint joinPoint;

    @Test
    void shouldReturnEmptyWhenTopLevelMatch() {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{ENTITY_ID, new FilingHistoryDeleteAggregate()
                .document(new FilingHistoryDocument()
                .entityId(ENTITY_ID)
                .data(new FilingHistoryData()))});

        // when
        Optional<FilingHistoryDocument> actual = aspect.deleteChildTransactionsDisabled(joinPoint);

        // then
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldThrowBadRequestExceptionWhenAnyChildMatch() {
        // given
        when(joinPoint.getArgs()).thenReturn(
                new Object[]{CHILD_ENTITY_ID, new FilingHistoryDeleteAggregate()
                        .document(new FilingHistoryDocument()
                                .entityId(ENTITY_ID))});

        // when
        Executable actual = () -> aspect.deleteChildTransactionsDisabled(joinPoint);

        // then
        assertThrows(BadRequestException.class, actual);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenCompositeResolutionMatch() {
        // given
        when(joinPoint.getArgs()).thenReturn(
                new Object[]{ENTITY_ID, new FilingHistoryDeleteAggregate()
                        .document(new FilingHistoryDocument()
                                .entityId(ENTITY_ID)
                        .data(new FilingHistoryData()
                                .type("RESOLUTIONS")))});

        // when
        Executable actual = () -> aspect.deleteChildTransactionsDisabled(joinPoint);

        // then
        assertThrows(BadRequestException.class, actual);
    }
}
