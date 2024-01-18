package uk.gov.companieshouse.filinghistory.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAnnotations;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryLinks;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DataMapperTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(ZoneId.of("Z"));
    private static final String TRANSACTION_ID = "transactionId";
    private static final String BARCODE = "barcode";
    private static final String TM01_TYPE = "TM01";
    private static final String COMPANY_NUMBER = "123456789";
    private static final String SELF_LINK = "/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID);
    private static final LocalDate DATE = LocalDate.parse("20150225185208001000", FORMATTER);
    private static final String ACTION_AND_TERMINATION_DATE_AS_STRING = "2015-01-25T18:52:08.001Z";
    private static final Instant ACTION_AND_TERMINATION_DATE_AS_INSTANT = Instant.parse(ACTION_AND_TERMINATION_DATE_AS_STRING);

    @InjectMocks
    private DataMapper dataMapper;

    @Test
    void mapFilingHistoryExternalDataShouldReturnFilingHistoryData() {
        // given
        final FilingHistoryData expectedData = new FilingHistoryData()
                .type(TM01_TYPE)
                .date(DATE.atStartOfDay(ZoneId.of("Z")).toInstant())
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
        final FilingHistoryData actualData = dataMapper.mapFilingHistoryExternalData(buildExternalData());

        // then
        assertEquals(expectedData, actualData);
    }

    private static ExternalData buildExternalData() {
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
                        .terminationDate(ACTION_AND_TERMINATION_DATE_AS_STRING)
                        .officerName("Officer Name"))
                .pages(1)
                .actionDate(LocalDate.parse("20150225185208001000", FORMATTER))
                .paperFiled(true)
                .links(new FilingHistoryItemDataLinks()
                        .documentMetadata("metadata")
                        .self(SELF_LINK));
    }

    private static List<FilingHistoryItemDataAnnotations> buildAnnotationsList() {
        List<FilingHistoryItemDataAnnotations> annotations = new ArrayList<>();
        annotations.add(new FilingHistoryItemDataAnnotations());
        return annotations;
    }
}
