package uk.gov.companieshouse.filinghistory.api.mapper;

import static java.time.ZoneOffset.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryLinks;

@ExtendWith(MockitoExtension.class)
class DataMapperTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String BARCODE = "barcode";
    private static final String TM01_TYPE = "TM01";
    private static final String COMPANY_NUMBER = "123456789";
    private static final String SELF_LINK = "/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID);
    private static final LocalDate DATE = LocalDate.of(2015, 1, 25);
    private static final LocalDate ACTION_AND_TERMINATION_DATE_AS_LOCAL_DATE = LocalDate.of(2015, 2, 26);
    private static final Instant ACTION_AND_TERMINATION_DATE_AS_INSTANT = Instant.from(ACTION_AND_TERMINATION_DATE_AS_LOCAL_DATE.atStartOfDay(UTC));

    @InjectMocks
    private DataMapper dataMapper;

    @Test
    void mapFilingHistoryExternalDataShouldReturnFilingHistoryData() {
        // given
        final FilingHistoryData expectedData = new FilingHistoryData()
                .type(TM01_TYPE)
                .date(DATE.atStartOfDay(UTC).toInstant())
                .category("officers")
                .subcategory("termination")
                .description("description")
                .descriptionValues(new FilingHistoryDescriptionValues()
                        .officerName("Officer Name")
                        .terminationDate(ACTION_AND_TERMINATION_DATE_AS_INSTANT))
                .actionDate(ACTION_AND_TERMINATION_DATE_AS_INSTANT)
                .pages(1)
                .paperFiled(true)
                .links(new FilingHistoryLinks()
                        .self(SELF_LINK)
                        .documentMetadata("metadata"));

        // when
        final FilingHistoryData actualData = dataMapper.mapFilingHistoryExternalData(buildRequestExternalData());

        // then
        assertEquals(expectedData, actualData);
    }

    private static ExternalData buildRequestExternalData() {
        return new ExternalData()
                .transactionId(TRANSACTION_ID)
                .barcode(BARCODE)
                .type(TM01_TYPE)
                .date(DATE)
                .category(ExternalData.CategoryEnum.OFFICERS)
                .annotations(null)
                .subcategory(ExternalData.SubcategoryEnum.TERMINATION)
                .description("description")
                .descriptionValues(new FilingHistoryItemDataDescriptionValues()
                        .terminationDate(ACTION_AND_TERMINATION_DATE_AS_LOCAL_DATE)
                        .officerName("Officer Name"))
                .pages(1)
                .actionDate(ACTION_AND_TERMINATION_DATE_AS_LOCAL_DATE)
                .paperFiled(true)
                .links(new FilingHistoryItemDataLinks()
                        .documentMetadata("metadata")
                        .self(SELF_LINK));
    }
}
