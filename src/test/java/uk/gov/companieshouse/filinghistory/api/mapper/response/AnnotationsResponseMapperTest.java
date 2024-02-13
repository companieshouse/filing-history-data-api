package uk.gov.companieshouse.filinghistory.api.mapper.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAnnotations;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;

class AnnotationsResponseMapperTest {

    private final AnnotationsResponseMapper annotationsResponseMapper = new AnnotationsResponseMapper();

    @Test
    void shouldSuccessfullyMapAnnotations() {
        // given
        final List<FilingHistoryItemDataAnnotations> expected = List.of(
                new FilingHistoryItemDataAnnotations().annotation("annotations_1"),
                new FilingHistoryItemDataAnnotations().annotation("annotations_2"));

        // when
        final List<FilingHistoryItemDataAnnotations> actual = annotationsResponseMapper.map(buildDocumentAnnotationsList());

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldSuccessfullyReturnNull() {
        // given

        // when
        final List<FilingHistoryItemDataAnnotations> actual = annotationsResponseMapper.map(null);

        // then
        assertNull(actual);
    }

    private static List<FilingHistoryAnnotation> buildDocumentAnnotationsList() {
        return List.of(
                new FilingHistoryAnnotation()
                        .annotation("annotations_1"),
                new FilingHistoryAnnotation()
                        .annotation("annotations_2"));
    }
}
