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
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataResolutions;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryResolution;

@ExtendWith(MockitoExtension.class)
class ResolutionsGetResponseMapperTest {

    private static final String CATEGORY = "category";
    private static final String TYPE = "type";
    private static final String DESCRIPTION = "description";

    @InjectMocks
    private ResolutionsGetResponseMapper resolutionsGetResponseMapper;

    @Mock
    private DescriptionValuesGetResponseMapper descriptionValuesGetResponseMapper;
    @Mock
    private FilingHistoryItemDataDescriptionValues filingHistoryItemDataDescriptionValues;

    @Test
    void shouldSuccessfullyMapResolutions() {
        // given
        final List<FilingHistoryItemDataResolutions> expected = List.of(
                new FilingHistoryItemDataResolutions()
                        .category(CATEGORY)
                        .type(TYPE)
                        .description(DESCRIPTION)
                        .descriptionValues(filingHistoryItemDataDescriptionValues));

        when(descriptionValuesGetResponseMapper.map(any())).thenReturn(filingHistoryItemDataDescriptionValues);

        // when
        final List<FilingHistoryItemDataResolutions> actual = resolutionsGetResponseMapper.map(buildDocumentResolutionsList());

        // then
        assertEquals(expected, actual);
        verify(descriptionValuesGetResponseMapper).map(new FilingHistoryDescriptionValues());
    }

    @Test
    void shouldSuccessfullyMapResolutionsWithNullDescriptionValues() {
        // given
        final List<FilingHistoryItemDataResolutions> expected = List.of(
                new FilingHistoryItemDataResolutions()
                        .category(CATEGORY)
                        .type(TYPE)
                        .description(DESCRIPTION));

        // when
        final List<FilingHistoryItemDataResolutions> actual = resolutionsGetResponseMapper.map(
                List.of(
                        new FilingHistoryResolution()
                                .category(CATEGORY)
                                .type(TYPE)
                                .description(DESCRIPTION))
        );

        // then
        assertEquals(expected, actual);
        verify(descriptionValuesGetResponseMapper).map(null);
    }

    @Test
    void shouldReturnNull() {
        // given

        // when
        final List<FilingHistoryItemDataResolutions> actual = resolutionsGetResponseMapper.map(null);

        // then
        assertNull(actual);
        verifyNoInteractions(descriptionValuesGetResponseMapper);
    }

    private static List<FilingHistoryResolution> buildDocumentResolutionsList() {
        return List.of(
                new FilingHistoryResolution()
                        .category(CATEGORY)
                        .type(TYPE)
                        .description(DESCRIPTION)
                        .descriptionValues(new FilingHistoryDescriptionValues()));
    }
}
