package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.AssociatedFiling;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryDocumentMetadataUpdateApi;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.api.filinghistory.Links;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAssociatedFiling;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryLinks;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryOriginalValues;

@ExtendWith(MockitoExtension.class)
class TopLevelTransactionMapperTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String ENTITY_ID = "1234567890";
    private static final String COMPANY_NUMBER = "123456789";
    private static final String DOCUMENT_ID = "documentId";
    private static final String BARCODE = "barcode";
    private static final String ORIGINAL_DESCRIPTION = "original description";
    private static final String EXISTING_DOCUMENT_DELTA_AT = "20140916230459600643";
    private static final String NEWEST_REQUEST_DELTA_AT = "20151025185208001000";
    private static final String STALE_REQUEST_DELTA_AT = "20130615185208001000";
    private static final Instant INSTANT = Instant.now();
    private static final String UPDATED_BY = "84746291";
    private static final String EXPECTED_DELTA_AT = NEWEST_REQUEST_DELTA_AT;
    private static final String DOC_METADATA_LINK = "/document/12345";
    private static final Integer PAGES = 5;

    @InjectMocks
    private TopLevelTransactionMapper topLevelMapper;
    @Mock
    private DataMapper dataMapper;
    @Mock
    private OriginalValuesMapper originalValuesMapper;
    @Mock
    private LinksMapper linksMapper;
    @Mock
    private ChildListMapper<FilingHistoryAssociatedFiling> childListMapper;

    @Mock
    private FilingHistoryData expectedFilingHistoryData;
    @Mock
    private FilingHistoryData existingFilingHistoryData;
    @Mock
    private FilingHistoryOriginalValues expectedFilingHistoryOriginalValues;
    @Mock
    private FilingHistoryOriginalValues existingFilingHistoryOriginalValues;
    @Mock
    private FilingHistoryLinks expectedFilingHistoryLinks;

    @Mock
    private InternalDataOriginalValues requestOriginalValues;
    @Mock
    private ExternalData requestExternalData;
    @Mock
    private Links requestLinks;


    @Test
    void mapNewFilingHistoryShouldReturnNewFilingHistoryDocument() {
        // given
        when(dataMapper.map((any()), any())).thenReturn(expectedFilingHistoryData);
        when(requestExternalData.getLinks()).thenReturn(requestLinks);
        when(linksMapper.map(any())).thenReturn(expectedFilingHistoryLinks);
        when(expectedFilingHistoryData.links(any())).thenReturn(expectedFilingHistoryData);
        when(requestExternalData.getPaperFiled()).thenReturn(true);
        when(expectedFilingHistoryData.paperFiled(any())).thenReturn(expectedFilingHistoryData);
        when(originalValuesMapper.map(any())).thenReturn(expectedFilingHistoryOriginalValues);
        when(requestExternalData.getBarcode()).thenReturn(BARCODE);

        final InternalFilingHistoryApi request = buildPutRequestBody();
        final FilingHistoryDocument expectedDocument = getFilingHistoryDocument(
                expectedFilingHistoryData,
                expectedFilingHistoryOriginalValues,
                EXPECTED_DELTA_AT);

        // when
        final FilingHistoryDocument actualDocument = topLevelMapper.mapNewFilingHistory(TRANSACTION_ID, request,
                INSTANT);

        // then
        assertEquals(expectedDocument, actualDocument);
        verifyNoInteractions(childListMapper);
        verify(expectedFilingHistoryData).paperFiled(true);
        verify(dataMapper).map(requestExternalData, new FilingHistoryData());
        verify(originalValuesMapper).map(requestOriginalValues);
        verify(linksMapper).map(requestLinks);
    }

    @Test
    void mapExistingFilingHistoryShouldReturnUpdatedFilingHistoryDocument() {
        // given
        when(dataMapper.map(any(), any())).thenReturn(expectedFilingHistoryData);
        when(requestExternalData.getPaperFiled()).thenReturn(true);
        when(expectedFilingHistoryData.paperFiled(any())).thenReturn(expectedFilingHistoryData);
        when(originalValuesMapper.map(any())).thenReturn(expectedFilingHistoryOriginalValues);
        when(requestExternalData.getBarcode()).thenReturn(BARCODE);
        when(requestExternalData.getAssociatedFilings()).thenReturn(null);

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
        FilingHistoryDocument actualDocument = topLevelMapper.mapExistingFilingHistory(request, existingDocument,
                INSTANT);

        // then
        assertEquals(expectedDocument, actualDocument);
        verifyNoInteractions(childListMapper);
        verify(expectedFilingHistoryData).paperFiled(true);
        verify(dataMapper).map(requestExternalData, existingFilingHistoryData);
        verifyNoInteractions(childListMapper);
        verify(originalValuesMapper).map(requestOriginalValues);
    }

    @Test
    void shouldUpdateExistingDocumentWhenExistingDocumentHasAssociatedFilings() {
        // given
        List<AssociatedFiling> requestAssociatedFilingList = List.of(new AssociatedFiling());
        FilingHistoryData expectedData = new FilingHistoryData()
                .associatedFilings(List.of(new FilingHistoryAssociatedFiling()));

        final InternalFilingHistoryApi request = buildPutRequestBody();
        final FilingHistoryDocument expectedDocument = getFilingHistoryDocument(
                expectedData,
                expectedFilingHistoryOriginalValues,
                EXPECTED_DELTA_AT);

        final FilingHistoryDocument existingDocument = getFilingHistoryDocument(
                existingFilingHistoryData,
                existingFilingHistoryOriginalValues,
                EXISTING_DOCUMENT_DELTA_AT);

        when(dataMapper.map(any(), any())).thenReturn(expectedData);
        when(originalValuesMapper.map(any())).thenReturn(expectedFilingHistoryOriginalValues);
        when(requestExternalData.getBarcode()).thenReturn(BARCODE);
        when(requestExternalData.getAssociatedFilings()).thenReturn(requestAssociatedFilingList);

        // when
        FilingHistoryDocument actualDocument = topLevelMapper.mapExistingFilingHistory(request, existingDocument,
                INSTANT);

        // then
        assertEquals(expectedDocument, actualDocument);
        verify(dataMapper).map(requestExternalData, existingFilingHistoryData);
        verify(childListMapper).mapChildList(eq(request), eq(expectedData.getAssociatedFilings()), any());
        verify(originalValuesMapper).map(requestOriginalValues);
    }

    @Test
    void shouldUpdateExistingDocumentWhenExistingDocumentHasEmptyAssociatedFilings() {
        // given
        List<AssociatedFiling> requestAssociatedFilingList = Collections.emptyList();
        FilingHistoryData expectedData = new FilingHistoryData()
                .associatedFilings(List.of(new FilingHistoryAssociatedFiling()));

        final InternalFilingHistoryApi request = buildPutRequestBody();
        final FilingHistoryDocument expectedDocument = getFilingHistoryDocument(
                expectedData,
                expectedFilingHistoryOriginalValues,
                EXPECTED_DELTA_AT);

        final FilingHistoryDocument existingDocument = getFilingHistoryDocument(
                existingFilingHistoryData,
                existingFilingHistoryOriginalValues,
                EXISTING_DOCUMENT_DELTA_AT);

        when(dataMapper.map(any(), any())).thenReturn(expectedData);
        when(originalValuesMapper.map(any())).thenReturn(expectedFilingHistoryOriginalValues);
        when(requestExternalData.getBarcode()).thenReturn(BARCODE);
        when(requestExternalData.getAssociatedFilings()).thenReturn(requestAssociatedFilingList);

        // when
        FilingHistoryDocument actualDocument = topLevelMapper.mapExistingFilingHistory(request, existingDocument,
                INSTANT);

        // then
        assertEquals(expectedDocument, actualDocument);
        verify(dataMapper).map(requestExternalData, existingFilingHistoryData);
        verifyNoInteractions(childListMapper);
        verify(originalValuesMapper).map(requestOriginalValues);
    }

    @Test
    void shouldThrowConflictExceptionWhenRequestDeltaAtIsStale() {
        // given
        final InternalFilingHistoryApi request = buildPutRequestBody();
        request.getInternalData().deltaAt(STALE_REQUEST_DELTA_AT);

        final FilingHistoryDocument existingDocument = getFilingHistoryDocument(
                existingFilingHistoryData,
                existingFilingHistoryOriginalValues,
                EXISTING_DOCUMENT_DELTA_AT);

        // when
        Executable executable = () -> topLevelMapper.mapExistingFilingHistory(request, existingDocument,
                INSTANT);

        // then
        assertThrows(ConflictException.class, executable);
        verifyNoInteractions(dataMapper);
        verifyNoInteractions(originalValuesMapper);
    }

    @Test
    void mapDocumentMetadataShouldReturnUpdatedFilingHistoryDocument() {
        // given
        final FilingHistoryDocumentMetadataUpdateApi request = buildPatchDocMetadataRequestBody();

        final FilingHistoryDocument existingDocument = getFilingHistoryDocument(
                new FilingHistoryData().links(new FilingHistoryLinks()),
                existingFilingHistoryOriginalValues,
                EXISTING_DOCUMENT_DELTA_AT);

        // when
        FilingHistoryDocument actualDocument = topLevelMapper.mapDocumentMetadata(request, existingDocument);

        // then
        assertEquals(existingDocument, actualDocument);
        assertEquals(PAGES, actualDocument.getData().getPages());
        assertEquals(DOC_METADATA_LINK, actualDocument.getData().getLinks().getDocumentMetadata());
    }

    private InternalFilingHistoryApi buildPutRequestBody() {
        return new InternalFilingHistoryApi()
                .externalData(requestExternalData)
                .internalData(buildInternalData());
    }

    private FilingHistoryDocumentMetadataUpdateApi buildPatchDocMetadataRequestBody() {
        return new FilingHistoryDocumentMetadataUpdateApi().documentMetadata(DOC_METADATA_LINK)
                .pages(PAGES);
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
                .updatedBy(UPDATED_BY);
    }

    private FilingHistoryDocument getFilingHistoryDocument(FilingHistoryData data,
                                                           FilingHistoryOriginalValues originalValues, String deltaAt) {
        FilingHistoryDeltaTimestamp timestamp = new FilingHistoryDeltaTimestamp()
                .at(INSTANT)
                .by(UPDATED_BY);
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
                .updated(timestamp)
                .created(timestamp);
    }
}
