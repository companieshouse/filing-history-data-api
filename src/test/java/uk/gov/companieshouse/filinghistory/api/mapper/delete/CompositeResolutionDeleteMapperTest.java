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
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

@ExtendWith(MockitoExtension.class)
class CompositeResolutionDeleteMapperTest {

    private static final Instant INSTANT = Instant.now();

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
        Optional<FilingHistoryDocument> actual = deleteMapper.removeTransaction(0, document);

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(instantSupplier).get();
    }

    @Test
    void shouldRemoveLastResolutionAtIndexAndReturnEmpty() {
        // given
        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .resolutions(new ArrayList<>(List.of(new FilingHistoryResolution()))));

        FilingHistoryDocument expected = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .resolutions(List.of()));
        // when
        Optional<FilingHistoryDocument> actual = deleteMapper.removeTransaction(0, document);

        // then
        assertTrue(actual.isEmpty());
        assertEquals(expected, document);
        verifyNoInteractions(instantSupplier);
    }
}