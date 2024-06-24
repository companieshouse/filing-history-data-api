package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.AssociatedFiling;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAssociatedFiling;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDescriptionValues;

@ExtendWith(MockitoExtension.class)
class AssociatedFilingsGetResponseMapperTest {

    private static final String CATEGORY = "category";
    private static final String SUBCATEGORY = "subcategory";
    private static final String TYPE = "type";
    private static final String DESCRIPTION = "description";
    private static final String ORIGINAL_DESCRIPTION = "original description";
    private static final String ACTION_DATE = "2015-10-05";

    @InjectMocks
    private AssociatedFilingsGetResponseMapper associatedFilingsGetResponseMapper;

    @Mock
    private DescriptionValuesGetResponseMapper descriptionValuesGetResponseMapper;
    @Mock
    private DescriptionValues descriptionValues;

    @Test
    void shouldSuccessfullyMapAssociatedFilings() {
        // given
        final List<AssociatedFiling> expected = List.of(
                new AssociatedFiling()
                        .actionDate(ACTION_DATE)
                        .category(CATEGORY)
                        .subcategory(SUBCATEGORY)
                        .type(TYPE)
                        .originalDescription(ORIGINAL_DESCRIPTION)
                        .description(DESCRIPTION)
                        .descriptionValues(descriptionValues));

        when(descriptionValuesGetResponseMapper.map(any())).thenReturn(descriptionValues);

        // when
        final List<AssociatedFiling> actual = associatedFilingsGetResponseMapper.map(buildDocumentAssociatedFilingsList());

        // then
        assertEquals(expected, actual);
        verify(descriptionValuesGetResponseMapper).map(new FilingHistoryDescriptionValues());
    }

    @Test
    void shouldSuccessfullyMapAssociatedFilingsWithNullDescriptionValues() {
        // given
        final List<AssociatedFiling> expected = List.of(
                new AssociatedFiling()
                        .category(CATEGORY)
                        .subcategory(SUBCATEGORY)
                        .type(TYPE)
                        .description(DESCRIPTION));

        // when
        final List<AssociatedFiling> actual = associatedFilingsGetResponseMapper.map(List.of(
                new FilingHistoryAssociatedFiling()
                        .category(CATEGORY)
                        .subcategory(SUBCATEGORY)
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
        final List<AssociatedFiling> actual = associatedFilingsGetResponseMapper.map(null);

        // then
        assertNull(actual);
        verifyNoInteractions(descriptionValuesGetResponseMapper);
    }

    private static List<FilingHistoryAssociatedFiling> buildDocumentAssociatedFilingsList() {
        return List.of(
                new FilingHistoryAssociatedFiling()
                        .actionDate(Instant.parse("2015-10-05T00:00:00Z"))
                        .category(CATEGORY)
                        .subcategory(SUBCATEGORY)
                        .type(TYPE)
                        .originalDescription(ORIGINAL_DESCRIPTION)
                        .description(DESCRIPTION)
                        .descriptionValues(new FilingHistoryDescriptionValues()));
    }
}
