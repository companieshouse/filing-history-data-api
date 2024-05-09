package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

class CompositeResolutionDeleteMapperTest {

    private final CompositeResolutionDeleteMapper deleteMapper = new CompositeResolutionDeleteMapper();

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
                        .resolutions(List.of(new FilingHistoryResolution())));

        // when
        Optional<FilingHistoryDocument> actual = deleteMapper.removeTransaction(0, document);

        // then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    @Test
    void shouldRemoveLastResolutionAtIndexAndReturnEmpty() {
        // given

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .resolutions(new ArrayList<>(List.of(new FilingHistoryResolution()))));

        // when
        Optional<FilingHistoryDocument> actual = deleteMapper.removeTransaction(0, document);

        // then
        assertTrue(actual.isEmpty());
    }
}