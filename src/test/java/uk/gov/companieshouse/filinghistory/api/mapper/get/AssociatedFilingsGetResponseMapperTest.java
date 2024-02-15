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
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAssociatedFilings;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAssociatedFiling;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;

@ExtendWith(MockitoExtension.class)
class AssociatedFilingsGetResponseMapperTest {

    private static final String CATEGORY = "category";
    private static final String TYPE = "type";
    private static final String DESCRIPTION = "description";

    @InjectMocks
    private AssociatedFilingsGetResponseMapper associatedFilingsGetResponseMapper;

    @Mock
    private DescriptionValuesGetResponseMapper descriptionValuesGetResponseMapper;
    @Mock
    private FilingHistoryItemDataDescriptionValues descriptionValues;

    @Test
    void shouldSuccessfullyMapAssociatedFilings() {
        // given
        final List<FilingHistoryItemDataAssociatedFilings> expected = List.of(
                new FilingHistoryItemDataAssociatedFilings()
                        .category(CATEGORY)
                        .type(TYPE)
                        .description(DESCRIPTION)
                        .descriptionValues(descriptionValues));

        when(descriptionValuesGetResponseMapper.map(any())).thenReturn(descriptionValues);

        // when
        final List<FilingHistoryItemDataAssociatedFilings> actual = associatedFilingsGetResponseMapper.map(buildDocumentAssociatedFilingsList());

        // then
        assertEquals(expected, actual);
        verify(descriptionValuesGetResponseMapper).map(new FilingHistoryDescriptionValues());
    }

    @Test
    void shouldReturnNull() {
        // given

        // when
        final List<FilingHistoryItemDataAssociatedFilings> actual = associatedFilingsGetResponseMapper.map(null);

        // then
        assertNull(actual);
        verifyNoInteractions(descriptionValuesGetResponseMapper);
    }

    private static List<FilingHistoryAssociatedFiling> buildDocumentAssociatedFilingsList() {
        return List.of(
                new FilingHistoryAssociatedFiling()
                        .category(CATEGORY)
                        .type(TYPE)
                        .description(DESCRIPTION)
                        .descriptionValues(new FilingHistoryDescriptionValues()));
    }
}
