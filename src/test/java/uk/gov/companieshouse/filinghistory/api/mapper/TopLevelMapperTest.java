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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
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
    public static final String EXISTING_DOCUMENT_DELTA_AT = "20140916230459600643";
    public static final OffsetDateTime NEWEST_REQUEST_DELTA_AT = OffsetDateTime.parse("2015-10-25T18:52:08.001Z");
    public static final OffsetDateTime STALE_REQUEST_DELTA_AT = OffsetDateTime.parse("2013-06-15T18:52:08.001Z");
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
    private FilingHistoryData staleFilingHistoryData;
    @Mock
    private FilingHistoryOriginalValues expectedFilingHistoryOriginalValues;
    @Mock
    private FilingHistoryOriginalValues staleFilingHistoryOriginalValues;

    @Mock
    private InternalDataOriginalValues requestOriginalValues;
    @Mock
    private ExternalData requestExternalData;


    @Test
    void mapNewFilingHistoryShouldReturnNewFilingHistoryDocumentWithMappedFields() {
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
                .deltaAt(FORMATTER.format(NEWEST_REQUEST_DELTA_AT))
                .updatedAt(UPDATED_AT)
                .updatedBy(UPDATED_BY);

        // when
        final FilingHistoryDocument actualDocument = topLevelMapper.mapNewFilingHistory(TRANSACTION_ID, request);

        // then
        assertEquals(expectedDocument, actualDocument);
    }

    @Test
    void mapFilingHistoryUnlessStaleShouldReturnAnOptionalOfAnUpdatedExistingDocumentWhenDeltaIsNotStale() {
        // given
        when(dataMapper.map(any())).thenReturn(expectedFilingHistoryData);
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
                .deltaAt(FORMATTER.format(NEWEST_REQUEST_DELTA_AT))
                .updatedAt(UPDATED_AT)
                .updatedBy(UPDATED_BY);

        final FilingHistoryDocument staleDocument = new FilingHistoryDocument()
                .transactionId(TRANSACTION_ID)
                .entityId(ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .documentId(DOCUMENT_ID)
                .barcode(BARCODE)
                .data(staleFilingHistoryData)
                .originalDescription(ORIGINAL_DESCRIPTION)
                .originalValues(staleFilingHistoryOriginalValues)
                .deltaAt(EXISTING_DOCUMENT_DELTA_AT)
                .updatedAt(UPDATED_AT)
                .updatedBy(UPDATED_BY);

        // when
        Optional<FilingHistoryDocument> actualDocument = topLevelMapper.mapFilingHistoryUnlessStale(request, staleDocument);

        // then
        assertTrue(actualDocument.isPresent());
        assertEquals(expectedDocument, actualDocument.get());
    }

    @Test
    void mapFilingHistoryUnlessStaleShouldReturnAnEmptyOptionalWhenDeltaIsStale() {
        // given
        final InternalFilingHistoryApi request = buildPutRequestBody();
        request.getInternalData().deltaAt(STALE_REQUEST_DELTA_AT);

        final FilingHistoryDocument existingDocument = new FilingHistoryDocument()
                .transactionId(TRANSACTION_ID)
                .entityId(ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .documentId(DOCUMENT_ID)
                .barcode(BARCODE)
                .data(staleFilingHistoryData)
                .originalDescription(ORIGINAL_DESCRIPTION)
                .originalValues(staleFilingHistoryOriginalValues)
                .deltaAt(EXISTING_DOCUMENT_DELTA_AT)
                .updatedAt(UPDATED_AT)
                .updatedBy(UPDATED_BY);

        // when
        Optional<FilingHistoryDocument> actualDocument = topLevelMapper.mapFilingHistoryUnlessStale(request, existingDocument);

        // then
        assertTrue(actualDocument.isEmpty());
        verifyNoInteractions(dataMapper);
        verifyNoInteractions(originalValuesMapper);
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
                .deltaAt(NEWEST_REQUEST_DELTA_AT)
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
