package uk.gov.companieshouse.filinghistory.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryOriginalValues;

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
    private static final String SELF_LINK = "/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
            .withZone(ZoneOffset.UTC);
    public static final String EXPECTED_DELTA_AT = FORMATTER.format(NEWEST_REQUEST_DELTA_AT);

    @InjectMocks
    private TopLevelMapper topLevelMapper;
    @Mock
    private DataMapper dataMapper;
    @Mock
    private OriginalValuesMapper originalValuesMapper;

    @Mock
    private FilingHistoryData expectedFilingHistoryData;
    @Mock
    private FilingHistoryData existingFilingHistoryData;
    @Mock
    private FilingHistoryOriginalValues expectedFilingHistoryOriginalValues;
    @Mock
    private FilingHistoryOriginalValues existingFilingHistoryOriginalValues;

    @Mock
    private InternalDataOriginalValues requestOriginalValues;
    @Mock
    private ExternalData requestExternalData;


    @Test
    void mapNewFilingHistoryShouldReturnNewFilingHistoryDocumentWithMappedFields() {
        // given
        when(dataMapper.mapFilingHistoryExternalData((any()))).thenReturn(expectedFilingHistoryData);
        when(originalValuesMapper.map(any())).thenReturn(expectedFilingHistoryOriginalValues);
        when(requestExternalData.getBarcode()).thenReturn(BARCODE);

        final InternalFilingHistoryApi request = buildPutRequestBody();
        final FilingHistoryDocument expectedDocument = getFilingHistoryDocument(
                expectedFilingHistoryData,
                expectedFilingHistoryOriginalValues,
                EXPECTED_DELTA_AT);

        // when
        final FilingHistoryDocument actualDocument = topLevelMapper.mapNewFilingHistory(TRANSACTION_ID, request);

        // then
        assertEquals(expectedDocument, actualDocument);
        verify(dataMapper).mapFilingHistoryExternalData(requestExternalData);
        verify(originalValuesMapper).map(requestOriginalValues);
    }

    @Test
    void mapFilingHistoryUnlessStaleShouldReturnAnOptionalOfAnUpdatedExistingDocumentWhenDeltaIsNotStale() {
        // TODO: Do not overwrite metadata link or child arrays
        // given
        when(dataMapper.mapFilingHistoryExternalData(any())).thenReturn(expectedFilingHistoryData);
        when(originalValuesMapper.map(any())).thenReturn(expectedFilingHistoryOriginalValues);
        when(requestExternalData.getBarcode()).thenReturn(BARCODE);

        final InternalFilingHistoryApi request = buildPutRequestBody();
        final FilingHistoryDocument expectedDocument = getFilingHistoryDocument(
                expectedFilingHistoryData,
                expectedFilingHistoryOriginalValues,
                EXPECTED_DELTA_AT);

        final FilingHistoryDocument existingDocument = getFilingHistoryDocument(
                existingFilingHistoryData,
                existingFilingHistoryOriginalValues,
                EXISTING_DOCUMENT_DELTA_AT);

        // when
        Optional<FilingHistoryDocument> actualDocument = topLevelMapper.mapFilingHistoryUnlessStale(request,
                existingDocument);

        // then
        assertTrue(actualDocument.isPresent());
        assertEquals(expectedDocument, actualDocument.get());
        verify(dataMapper).mapFilingHistoryExternalData(requestExternalData);
        verify(originalValuesMapper).map(requestOriginalValues);
    }

    @Test
    void mapFilingHistoryUnlessStaleShouldReturnAnEmptyOptionalWhenDeltaIsStale() {
        // given
        final InternalFilingHistoryApi request = buildPutRequestBody();
        request.getInternalData().deltaAt(STALE_REQUEST_DELTA_AT);

        final FilingHistoryDocument existingDocument = getFilingHistoryDocument(
                existingFilingHistoryData,
                existingFilingHistoryOriginalValues,
                EXISTING_DOCUMENT_DELTA_AT);

        // when
        Optional<FilingHistoryDocument> actualDocument = topLevelMapper.mapFilingHistoryUnlessStale(request,
                existingDocument);

        // then
        assertTrue(actualDocument.isEmpty());
        verifyNoInteractions(dataMapper);
        verifyNoInteractions(originalValuesMapper);
    }

    private InternalFilingHistoryApi buildPutRequestBody() {
        return new InternalFilingHistoryApi()
                .externalData(requestExternalData)
                .internalData(buildInternalData());
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

    private FilingHistoryDocument getFilingHistoryDocument(FilingHistoryData data,
            FilingHistoryOriginalValues originalValues, String deltaAt) {
        return new FilingHistoryDocument()
                .transactionId(TRANSACTION_ID)
                .entityId(ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .documentId(DOCUMENT_ID)
                .barcode(BARCODE)
                .data(data)
                .originalDescription(ORIGINAL_DESCRIPTION)
                .originalValues(originalValues)
                .deltaAt(deltaAt)
                .updatedAt(UPDATED_AT)
                .updatedBy(UPDATED_BY);
    }
}
