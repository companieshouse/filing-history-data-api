package uk.gov.companieshouse.filinghistory.api.mapper;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
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
    private static final LocalDate DATE = LocalDate.of(2015, 1, 25);
    private static final LocalDate ACTION_AND_TERMINATION_DATE_AS_LOCAL_DATE = LocalDate.of(2015, 2, 26);
    private static final Instant ACTION_AND_TERMINATION_DATE_AS_INSTANT = Instant.from(
            ACTION_AND_TERMINATION_DATE_AS_LOCAL_DATE.atStartOfDay(UTC));

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
                .date(DATE.atStartOfDay(UTC).toInstant())
                .category("officers")
                .subcategory("termination")
                .description("description")
                .descriptionValues(expectedDescriptionValues)
                .actionDate(ACTION_AND_TERMINATION_DATE_AS_INSTANT)
                .pages(1)
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
                .date(DATE.atStartOfDay(UTC).toInstant())
                .category("officers")
                .subcategory("termination")
                .description("description")
                .descriptionValues(expectedDescriptionValues)
                .annotations(List.of(new FilingHistoryAnnotation().annotation("annotation")))
                .actionDate(ACTION_AND_TERMINATION_DATE_AS_INSTANT)
                .pages(1)
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
                .pages(1)
                .actionDate(ACTION_AND_TERMINATION_DATE_AS_LOCAL_DATE)
                .paperFiled(true)
                .links(requestLinks);
    }
}
