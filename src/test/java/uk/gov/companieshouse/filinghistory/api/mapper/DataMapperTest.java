package uk.gov.companieshouse.filinghistory.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;

@ExtendWith(MockitoExtension.class)
class DataMapperTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String BARCODE = "barcode";
    private static final String TM01_TYPE = "TM01";
    private static final String DATE = "20150125000000";
    private static final Instant DATE_AS_INSTANT = Instant.parse("2015-01-25T00:00:00.00Z");
    private static final String ACTION_AND_TERMINATION_DATE_AS_LOCAL_DATE = "20150226000000";
    private static final Instant ACTION_AND_TERMINATION_DATE_AS_INSTANT = Instant.parse(
            "2015-02-26T00:00:00.00Z");

    @InjectMocks
    private DataMapper dataMapper;
    @Mock
    private DescriptionValuesMapper descriptionValuesMapper;

    @Mock
    private FilingHistoryItemDataDescriptionValues requestDescriptionValues;
    @Mock
    private FilingHistoryDescriptionValues expectedDescriptionValues;

    @Mock
    private FilingHistoryItemDataLinks requestLinks;

    @Test
    void mapShouldReturnFilingHistoryDataWhenNewData() {
        // given
        when(descriptionValuesMapper.map(any())).thenReturn(expectedDescriptionValues);

        final FilingHistoryData expectedData = new FilingHistoryData()
                .type(TM01_TYPE)
                .date(DATE_AS_INSTANT)
                .category("officers")
                .subcategory("termination")
                .description("description")
                .descriptionValues(expectedDescriptionValues)
                .actionDate(ACTION_AND_TERMINATION_DATE_AS_INSTANT)
                .paperFiled(true);

        // when
        final FilingHistoryData actualData = dataMapper.map(buildRequestExternalData(), new FilingHistoryData());

        // then
        assertEquals(expectedData, actualData);
        verify(descriptionValuesMapper).map(requestDescriptionValues);
    }

    @Test
    void mapShouldNotOverwriteDataWhenPassedExistingData() {
        // given
        when(descriptionValuesMapper.map(any())).thenReturn(expectedDescriptionValues);

        final FilingHistoryData expectedData = new FilingHistoryData()
                .type(TM01_TYPE)
                .date(DATE_AS_INSTANT)
                .category("officers")
                .subcategory("termination")
                .description("description")
                .descriptionValues(expectedDescriptionValues)
                .annotations(List.of(new FilingHistoryAnnotation().annotation("annotation")))
                .actionDate(ACTION_AND_TERMINATION_DATE_AS_INSTANT)
                .paperFiled(true);

        final FilingHistoryData existingData = new FilingHistoryData()
                .annotations(List.of(new FilingHistoryAnnotation().annotation("annotation")));

        // when
        final FilingHistoryData actualData = dataMapper.map(buildRequestExternalData(), existingData);

        // then
        assertEquals(expectedData, actualData);
        verify(descriptionValuesMapper).map(requestDescriptionValues);
    }

    private ExternalData buildRequestExternalData() {
        return new ExternalData()
                .transactionId(TRANSACTION_ID)
                .barcode(BARCODE)
                .type(TM01_TYPE)
                .date(DATE)
                .category(ExternalData.CategoryEnum.OFFICERS)
                .annotations(null)
                .subcategory(ExternalData.SubcategoryEnum.TERMINATION)
                .description("description")
                .descriptionValues(requestDescriptionValues)
                .pages(1) // should not be mapped, persisted by document store sub delta
                .actionDate(ACTION_AND_TERMINATION_DATE_AS_LOCAL_DATE)
                .paperFiled(true)
                .links(requestLinks);
    }
}
