package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.AssociatedFiling;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;

@ExtendWith(MockitoExtension.class)
class FilingHistoryItemCleanserTest {

    private static final String NEW_INC = "NEWINC";
    @InjectMocks
    private FilingHistoryItemCleanser filingHistoryItemCleanser;
    @Mock
    private AssociatedFilingCleanser associatedFilingCleanser;
    @Mock
    private DescriptionValuesCleanser descriptionValuesCleanser;

    @Mock
    private AssociatedFiling filingsWithDuplicates;
    @Mock
    private AssociatedFiling cleanFilings;
    @Mock
    private DescriptionValues valuesWithBackslashes;
    @Mock
    private DescriptionValues cleanDescriptionValues;

    @Test
    void shouldCleanseAssociatedFilings() {
        // given
        when(associatedFilingCleanser.removeDuplicateModelArticles(any(), any())).thenReturn(List.of(cleanFilings));

        ExternalData externalData = new ExternalData()
                .type(NEW_INC)
                .associatedFilings(List.of(filingsWithDuplicates));

        ExternalData expected = new ExternalData()
                .type(NEW_INC)
                .associatedFilings(List.of(cleanFilings));

        // when
        ExternalData actual = filingHistoryItemCleanser.cleanseFilingHistoryItem(externalData);

        // then
        assertEquals(expected, actual);
        verify(associatedFilingCleanser).removeDuplicateModelArticles(NEW_INC, List.of(filingsWithDuplicates));
    }

    @Test
    void shouldCleanseDescriptionValues() {
        // given
        when(descriptionValuesCleanser.replaceBackslashesWithWhitespace(any(), any()))
                .thenReturn(cleanDescriptionValues);

        ExternalData externalData = new ExternalData()
                .category(CategoryEnum.ADDRESS)
                .descriptionValues(valuesWithBackslashes);

        ExternalData expected = new ExternalData()
                .category(CategoryEnum.ADDRESS)
                .descriptionValues(cleanDescriptionValues);

        // when
        ExternalData actual = filingHistoryItemCleanser.cleanseFilingHistoryItem(externalData);

        // then
        assertEquals(expected, actual);
        verify(descriptionValuesCleanser).replaceBackslashesWithWhitespace(CategoryEnum.ADDRESS, valuesWithBackslashes);
    }
}