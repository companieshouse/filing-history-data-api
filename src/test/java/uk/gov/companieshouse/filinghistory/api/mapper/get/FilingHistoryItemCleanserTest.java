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
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAssociatedFilings;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;

@ExtendWith(MockitoExtension.class)
class FilingHistoryItemCleanserTest {

    @InjectMocks
    private FilingHistoryItemCleanser filingHistoryItemCleanser;
    @Mock
    private AssociatedFilingCleanser associatedFilingCleanser;
    @Mock
    private DescriptionValuesCleanser descriptionValuesCleanser;

    @Mock
    private FilingHistoryItemDataAssociatedFilings filingsWithDuplicates;
    @Mock
    private FilingHistoryItemDataAssociatedFilings filingsDeDuplicated;
    @Mock
    private FilingHistoryItemDataAssociatedFilings cleanFilings;
    @Mock
    private FilingHistoryItemDataDescriptionValues valuesWithBackslashes;
    @Mock
    private FilingHistoryItemDataDescriptionValues cleanDescriptionValues;

    @Test
    void shouldCleanseAssociatedFilings() {
        // given
        when(associatedFilingCleanser.removeDuplicateModelArticles(any())).thenReturn(List.of(filingsDeDuplicated));
        when(associatedFilingCleanser.removeOriginalDescription(any()))
                .thenReturn(List.of(cleanFilings));

        ExternalData externalData = new ExternalData()
                .associatedFilings(List.of(filingsWithDuplicates));

        ExternalData expected = new ExternalData()
                .associatedFilings(List.of(cleanFilings));

        // when
        ExternalData actual = filingHistoryItemCleanser.cleanseFilingHistoryItem(externalData);

        // then
        assertEquals(expected, actual);
        verify(associatedFilingCleanser).removeDuplicateModelArticles(List.of(filingsWithDuplicates));
        verify(associatedFilingCleanser).removeOriginalDescription(List.of(filingsDeDuplicated));
    }

    @Test
    void shouldCleanseTopLevelAnnotations() {
        // given
        ExternalData externalData = new ExternalData()
                .type("ANNOTATION")
                .annotations(List.of());

        ExternalData expected = new ExternalData()
                .type("ANNOTATION");

        // when
        ExternalData actual = filingHistoryItemCleanser.cleanseFilingHistoryItem(externalData);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldCleanseDescriptionValues() {
        // given
        when(descriptionValuesCleanser.replaceBackslashesWithWhiteSpace(any(), any()))
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
        verify(descriptionValuesCleanser).replaceBackslashesWithWhiteSpace(CategoryEnum.ADDRESS, valuesWithBackslashes);
    }
}