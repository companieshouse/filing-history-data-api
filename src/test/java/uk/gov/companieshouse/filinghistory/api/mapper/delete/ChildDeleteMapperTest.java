package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAssociatedFiling;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

@ExtendWith(MockitoExtension.class)
class ChildDeleteMapperTest {

    private static final Instant INSTANT = Instant.now();
    private static final String ENTITY_ID = "entity ID";
    private static final String PARENT_TYPE = "parent type";

    @InjectMocks
    private ChildDeleteMapper deleteMapper;
    @Mock
    private Supplier<Instant> instantSupplier;

    @BeforeEach
    void setUp() {
        DataMapHolder.clear();
    }

    @Test
    void shouldReturnEmptyWhenEdgeCaseTopLevelTransaction() {
        // given
        FilingHistoryData data = new FilingHistoryData()
                .resolutions(new ArrayList<>(List.of(new FilingHistoryResolution()
                        .entityId(ENTITY_ID))));

        FilingHistoryDocument documentCopy = new FilingHistoryDocument()
                .entityId(ENTITY_ID)
                .data(data);
        // when
        Optional<FilingHistoryDocument> actual = deleteMapper.removeTransaction(ENTITY_ID, 0, documentCopy,
                data::getResolutions, data::resolutions);

        // then
        assertTrue(actual.isEmpty());
        verifyNoInteractions(instantSupplier);
    }

    @Test
    void shouldReturnEmptyWhenChildTransactionWithNoParentType() {
        // given
        FilingHistoryData data = new FilingHistoryData()
                .resolutions(new ArrayList<>(List.of(new FilingHistoryResolution()
                        .entityId(ENTITY_ID))));

        FilingHistoryDocument documentCopy = new FilingHistoryDocument()
                .data(data);
        // when
        Optional<FilingHistoryDocument> actual = deleteMapper.removeTransaction(ENTITY_ID, 0, documentCopy,
                data::getResolutions, data::resolutions);

        // then
        assertTrue(actual.isEmpty());
        verifyNoInteractions(instantSupplier);
    }

    @Test
    void shouldReturnUpdatedDocumentWithResolutionsArrayNullWhenLastChild() {
        // given
        FilingHistoryData data = new FilingHistoryData()
                .type(PARENT_TYPE)
                .resolutions(new ArrayList<>(List.of(new FilingHistoryResolution()
                        .entityId(ENTITY_ID))));

        FilingHistoryDocument documentCopy = new FilingHistoryDocument()
                .data(data);

        FilingHistoryDocument expected = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .type(PARENT_TYPE))
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(INSTANT)
                        .by("uninitialised"));

        when(instantSupplier.get()).thenReturn(INSTANT);

        // when
        Optional<FilingHistoryDocument> actual = deleteMapper.removeTransaction(ENTITY_ID, 0, documentCopy,
                data::getResolutions, data::resolutions);

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(instantSupplier).get();
    }

    @Test
    void shouldReturnUpdatedDocumentWithAnnotationsArrayNullWhenLastChild() {
        // given
        FilingHistoryData data = new FilingHistoryData()
                .type(PARENT_TYPE)
                .annotations(new ArrayList<>(List.of(new FilingHistoryAnnotation()
                        .entityId(ENTITY_ID))));

        FilingHistoryDocument documentCopy = new FilingHistoryDocument()
                .data(data);

        FilingHistoryDocument expected = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .type(PARENT_TYPE))
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(INSTANT)
                        .by("uninitialised"));

        when(instantSupplier.get()).thenReturn(INSTANT);

        // when
        Optional<FilingHistoryDocument> actual = deleteMapper.removeTransaction(ENTITY_ID, 0, documentCopy,
                data::getAnnotations, data::annotations);

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(instantSupplier).get();
    }

    @Test
    void shouldReturnUpdatedDocumentWithAssociatedFilingsArrayNullWhenLastChild() {
        // given
        FilingHistoryData data = new FilingHistoryData()
                .type(PARENT_TYPE)
                .associatedFilings(new ArrayList<>(List.of(new FilingHistoryAssociatedFiling()
                        .entityId(ENTITY_ID))));

        FilingHistoryDocument documentCopy = new FilingHistoryDocument()
                .data(data);

        FilingHistoryDocument expected = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .type(PARENT_TYPE))
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(INSTANT)
                        .by("uninitialised"));

        when(instantSupplier.get()).thenReturn(INSTANT);

        // when
        Optional<FilingHistoryDocument> actual = deleteMapper.removeTransaction(ENTITY_ID, 0, documentCopy,
                data::getAssociatedFilings, data::associatedFilings);

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(instantSupplier).get();
    }

    @Test
    void shouldRemoveOneResolutionAtIndexAndReturnUpdatedDocument() {
        // given
        FilingHistoryData data = new FilingHistoryData()
                .resolutions(new ArrayList<>(
                        List.of(
                                new FilingHistoryResolution()
                                        .entityId(ENTITY_ID),
                                new FilingHistoryResolution())));

        FilingHistoryDocument documentCopy = new FilingHistoryDocument()
                .data(data);

        FilingHistoryDocument expected = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .resolutions(List.of(new FilingHistoryResolution())))
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(INSTANT)
                        .by("uninitialised"));

        when(instantSupplier.get()).thenReturn(INSTANT);

        // when
        Optional<FilingHistoryDocument> actual = deleteMapper.removeTransaction(ENTITY_ID, 0, documentCopy,
                data::getResolutions, data::resolutions);

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(instantSupplier).get();
    }
}