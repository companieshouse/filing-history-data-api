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
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeleteAggregate;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@ExtendWith(MockitoExtension.class)
class DeleteMapperDelegatorAspectTest {

    private final DeleteMapperDelegatorAspect aspect = new DeleteMapperDelegatorAspect();

    @Mock
    private JoinPoint joinPoint;

    @Test
    void shouldReturnEmptyWhenNoChildMatches() {
        // given
        when(joinPoint.getArgs()).thenReturn(new Object[]{"entity id", new FilingHistoryDeleteAggregate()});

        // when
        Optional<FilingHistoryDocument> actual = aspect.deleteChildTransactionsDisabled(joinPoint);

        // then
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldThrowBadRequestExceptionWhenResolutionChildMatch() {
        // given
        when(joinPoint.getArgs()).thenReturn(
                new Object[]{"entity id", new FilingHistoryDeleteAggregate().resolutionIndex(0)});

        // when
        Executable actual = () -> aspect.deleteChildTransactionsDisabled(joinPoint);

        // then
        assertThrows(BadRequestException.class, actual);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenAnnotationChildMatch() {
        // given
        when(joinPoint.getArgs()).thenReturn(
                new Object[]{"entity id", new FilingHistoryDeleteAggregate().annotationIndex(0)});

        // when
        Executable actual = () -> aspect.deleteChildTransactionsDisabled(joinPoint);

        // then
        assertThrows(BadRequestException.class, actual);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenAssociatedFilingChildMatch() {
        // given
        when(joinPoint.getArgs()).thenReturn(
                new Object[]{"entity id", new FilingHistoryDeleteAggregate().associatedFilingIndex(0)});

        // when
        Executable actual = () -> aspect.deleteChildTransactionsDisabled(joinPoint);

        // then
        assertThrows(BadRequestException.class, actual);
    }
}
