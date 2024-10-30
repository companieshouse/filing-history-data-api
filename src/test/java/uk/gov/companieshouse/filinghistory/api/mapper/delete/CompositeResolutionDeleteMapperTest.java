package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

@ExtendWith(MockitoExtension.class)
class CompositeResolutionDeleteMapperTest {
    private static final Instant INSTANT = Instant.now();
    private static final String ENTITY_ID = "entity ID";
    private static final String DELTA_AT = "20151025185208001000";
    private static final String STALE_DELTA_AT = "20141025185208001000";

    @InjectMocks
    private CompositeResolutionDeleteMapper deleteMapper;
    @Mock
    private Supplier<Instant> instantSupplier;

    @BeforeEach
    void setUp() {
        DataMapHolder.clear();
    }

    @Test
    void shouldRemoveResolutionAtIndexAndReturnUpdatedDocument() {
        // given
        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .resolutions(new ArrayList<>(
                                List.of(
                                        new FilingHistoryResolution()
                                                .entityId("entity ID"),
                                        new FilingHistoryResolution()))));

        FilingHistoryDocument expected = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .resolutions(List.of(new FilingHistoryResolution())))
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(INSTANT)
                        .by("uninitialised"));

        when(instantSupplier.get()).thenReturn(INSTANT);

        // when
        Optional<FilingHistoryDocument> actual = deleteMapper.removeTransaction(0, DELTA_AT, document);

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(instantSupplier).get();
    }

    @Test
    void shouldReturnEmptyWhenLastCompositeResolution() {
        // given
        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .resolutions(new ArrayList<>(List.of(new FilingHistoryResolution()))));

        // when
        Optional<FilingHistoryDocument> actual = deleteMapper.removeTransaction(0, DELTA_AT, document);

        // then
        assertTrue(actual.isEmpty());
        verifyNoInteractions(instantSupplier);
    }

    @Test
    void shouldNotRemoveResolutionAtIndexWhenDeltaStaleAndReturn409() {
        // given
        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .resolutions(new ArrayList<>(
                                List.of(
                                        new FilingHistoryResolution()
                                                .entityId(ENTITY_ID)
                                                .deltaAt(DELTA_AT),
                                        new FilingHistoryResolution()
                                                .entityId(ENTITY_ID)
                                                .deltaAt(DELTA_AT)))));

        // when
        Executable actual = () -> deleteMapper.removeTransaction(0, STALE_DELTA_AT, document);

        // then
        assertThrows(ConflictException.class, actual);
    }
}