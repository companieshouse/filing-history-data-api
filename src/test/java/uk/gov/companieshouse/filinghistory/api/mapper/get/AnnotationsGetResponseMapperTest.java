package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAnnotations;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;

@ExtendWith(MockitoExtension.class)
class AnnotationsGetResponseMapperTest {

    @InjectMocks
    private AnnotationsGetResponseMapper annotationsGetResponseMapper;

    @Mock
    private DescriptionValuesGetResponseMapper descriptionValuesGetResponseMapper;

    @Test
    void shouldSuccessfullyMapAnnotations() {
        // given
        final List<FilingHistoryItemDataAnnotations> expected = List.of(
                new FilingHistoryItemDataAnnotations()
                        .annotation("annotations_1")
                        .descriptionValues(new FilingHistoryItemDataDescriptionValues()),
                new FilingHistoryItemDataAnnotations()
                        .annotation("annotations_2")
                        .descriptionValues(new FilingHistoryItemDataDescriptionValues()));

        when(descriptionValuesGetResponseMapper.map(any())).thenReturn(new FilingHistoryItemDataDescriptionValues());

        // when
        final List<FilingHistoryItemDataAnnotations> actual = annotationsGetResponseMapper.map(buildDocumentAnnotationsList());

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldSuccessfullyReturnNull() {
        // given

        // when
        final List<FilingHistoryItemDataAnnotations> actual = annotationsGetResponseMapper.map(null);

        // then
        assertNull(actual);
    }

    private static List<FilingHistoryAnnotation> buildDocumentAnnotationsList() {
        return List.of(
                (FilingHistoryAnnotation) new FilingHistoryAnnotation()
                        .annotation("annotations_1")
                        .descriptionValues(new FilingHistoryDescriptionValues()),
                (FilingHistoryAnnotation) new FilingHistoryAnnotation()
                        .annotation("annotations_2")
                        .descriptionValues(new FilingHistoryDescriptionValues()));
    }
}
