package uk.gov.companieshouse.filinghistory.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.*;
import uk.gov.companieshouse.filinghistory.api.model.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopLevelMapperTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String ENTITY_ID = "1234567890";
    private static final String COMPANY_NUMBER = "123456789";
    private static final String DOCUMENT_ID = "documentId";
    private static final String BARCODE = "barcode";
    private static final String TM01_TYPE = "TM01";
    private static final String ORIGINAL_DESCRIPTION = "original description";
    public static final String DELTA_AT = "20140916230459600643";
    private static final Instant UPDATED_AT = Instant.parse("2015-10-25T18:52:08.001Z");
    private static final String UPDATED_BY = "84746291";
    private static final String SELF_LINK = "company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(ZoneId.of("Z"));

    @InjectMocks
    private TopLevelMapper topLevelMapper;
    @Mock
    private DataMapper dataMapper;
    @Mock
    private OriginalValuesMapper originalValuesMapper;

    @Mock
    private FilingHistoryData expectedFilingHistoryData;
    @Mock
    private FilingHistoryOriginalValues expectedFilingHistoryOriginalValues;

    @Mock
    private InternalDataOriginalValues requestOriginalValues;
    @Mock
    private ExternalData requestExternalData;


    @Test
    void shouldMapPutRequestOntoMongoDocumentWhenNoRecordExistsInDB() {
        // given
        when(dataMapper.map((any()))).thenReturn(expectedFilingHistoryData);
        when(originalValuesMapper.map(any())).thenReturn(expectedFilingHistoryOriginalValues);
        when(requestExternalData.getBarcode()).thenReturn(BARCODE);

        final InternalFilingHistoryApi request = buildPutRequestBody();
        final FilingHistoryDocument expectedDocument = new FilingHistoryDocument()
                .transactionId(TRANSACTION_ID)
                .entityId(ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .documentId(DOCUMENT_ID)
                .barcode(BARCODE)
                .data(expectedFilingHistoryData)
                .originalDescription(ORIGINAL_DESCRIPTION)
                .originalValues(expectedFilingHistoryOriginalValues)
                .deltaAt(DELTA_AT)
                .updatedAt(UPDATED_AT)
                .updatedBy(UPDATED_BY);

        // when
        final FilingHistoryDocument actualDocument = topLevelMapper.mapFilingHistory(TRANSACTION_ID, request);

        // then
        assertEquals(expectedDocument, actualDocument);
    }

    private static ExternalData buildExternalData() {
        return new ExternalData()
                .transactionId(TRANSACTION_ID)
                .barcode(BARCODE)
                .type(TM01_TYPE)
                .date(LocalDate.parse("20150225185208001000", FORMATTER))
                .category(ExternalData.CategoryEnum.OFFICERS)
                .annotations(buildAnnotationsList())
                .subcategory(ExternalData.SubcategoryEnum.TERMINATION)
                .description("description")
                .descriptionValues(new FilingHistoryItemDataDescriptionValues()
                        .terminationDate("2015-01-25T18:52:08.001Z")
                        .officerName("Officer Name"))
                .pages(1)
                .actionDate(LocalDate.parse("20150225185208001000", FORMATTER))
                .paperFiled(true)
                .links(new FilingHistoryItemDataLinks()
                        .documentMetadata("metadata")
                        .self(SELF_LINK));
    }

    private InternalData buildInternalData() {
        return new InternalData()
                .entityId(ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .documentId(DOCUMENT_ID)
                .deltaAt(OffsetDateTime.parse(DELTA_AT, FORMATTER))
                .originalDescription("original description")
                .originalValues(requestOriginalValues)
                .parentEntityId("parent_entity_id")
                .updatedAt("2015-10-25T18:52:08.001Z")
                .updatedBy(UPDATED_BY);
    }

    private InternalFilingHistoryApi buildPutRequestBody() {
        return new InternalFilingHistoryApi()
                .externalData(requestExternalData)
                .internalData(buildInternalData());
    }

    private static FilingHistoryData buildFilingHistoryData() {
        return new FilingHistoryData()
                .type(TM01_TYPE)
                .date(Instant.parse("2015-02-25T18:52:08.001Z"))
                .category("officers")
                .subcategory("termination")
                .description("description")
                .descriptionValues(new FilingHistoryDescriptionValues()
                        .terminationDate(Instant.parse("2015-01-25T18:52:08.001Z"))
                        .officerName("Officer Name"))
                .actionDate(Instant.parse("2015-01-25T18:52:08.001Z"))
                .pages(1)
                .paperFiled(true)
                .links(new FilingHistoryLinks()
                        .self(SELF_LINK)
                        .documentMetadata("metadata"));
    }

    private static FilingHistoryOriginalValues buildFilingHistoryOriginalValues() {
        return new FilingHistoryOriginalValues()
                .officerName("Officer Name")
                .resignationDate("29/08/2014");
    }

    private static List<FilingHistoryItemDataAnnotations> buildAnnotationsList() {
        List<FilingHistoryItemDataAnnotations> annotations = new ArrayList<>();
        annotations.add(new FilingHistoryItemDataAnnotations());
        return annotations;
    }
}
