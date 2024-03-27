package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.Annotation;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDescriptionValues;

@ExtendWith(MockitoExtension.class)
class AnnotationsGetResponseMapperTest {

    private static final String CATEGORY = "category";
    private static final String TYPE = "type";
    private static final String DESCRIPTION = "description";

    @InjectMocks
    private AnnotationsGetResponseMapper annotationsGetResponseMapper;

    @Mock
    private DescriptionValuesGetResponseMapper descriptionValuesGetResponseMapper;
    @Mock
    private DescriptionValues DescriptionValues;

    @Test
    void shouldSuccessfullyMapAnnotations() {
        // given
        final List<Annotation> expected = List.of(
                new Annotation()
                        .annotation("annotations_1")
                        .category(CATEGORY)
                        .type(TYPE)
                        .description(DESCRIPTION)
                        .descriptionValues(DescriptionValues));

        when(descriptionValuesGetResponseMapper.map(any())).thenReturn(DescriptionValues);

        // when
        final List<Annotation> actual = annotationsGetResponseMapper.map(buildDocumentAnnotationsList());

        // then
        assertEquals(expected, actual);
        verify(descriptionValuesGetResponseMapper).map(new FilingHistoryDescriptionValues());
    }

    @Test
    void shouldSuccessfullyMapAnnotationsWithNulLDescriptionValues() {
        // given
        final List<Annotation> expected = List.of(
                new Annotation()
                        .annotation("annotations_1")
                        .category(CATEGORY)
                        .type(TYPE)
                        .description(DESCRIPTION));

        // when
        final List<Annotation> actual = annotationsGetResponseMapper.map(List.of(
                new FilingHistoryAnnotation()
                        .annotation("annotations_1")
                        .category(CATEGORY)
                        .type(TYPE)
                        .description(DESCRIPTION)));

        // then
        assertEquals(expected, actual);
        verify(descriptionValuesGetResponseMapper).map(null);
    }

    @Test
    void shouldReturnNull() {
        // given

        // when
        final List<Annotation> actual = annotationsGetResponseMapper.map(null);

        // then
        assertNull(actual);
        verifyNoInteractions(descriptionValuesGetResponseMapper);
    }

    private static List<FilingHistoryAnnotation> buildDocumentAnnotationsList() {
        return List.of(
                new FilingHistoryAnnotation()
                        .annotation("annotations_1")
                        .category(CATEGORY)
                        .type(TYPE)
                        .description(DESCRIPTION)
                        .descriptionValues(new FilingHistoryDescriptionValues()));
    }
}
