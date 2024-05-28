package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.api.filinghistory.Links;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryLinks;

@ExtendWith(MockitoExtension.class)
class AnnotationTransactionMapperTest {

    private static final String TRANSACTION_ID = "transaction ID";
    private static final String ENTITY_ID = "entity ID";
    private static final String PARENT_ENTITY_ID = "parent entity ID";
    private static final String COMPANY_NUMBER = "company number";
    private static final String DOCUMENT_ID = "document ID";
    private static final String DELTA_AT = "delta at";
    private static final String ORIGINAL_DESCRIPTION = "original description";
    private static final int MATCHED_DEFAULT = 1;
    private static final String BARCODE = "barcode";
    private static final Instant UPDATED_AT = Instant.parse("2024-05-02T00:00:00Z");
    private static final String UPDATED_BY = "updated by";
    private static final String DATE = "2011-11-26T11:27:55.000Z";
    private static final Instant CREATED_AT = Instant.parse(DATE);
    private static final String CREATED_BY = "created by";

    @InjectMocks
    private AnnotationTransactionMapper annotationTransactionMapper;
    @Mock
    private LinksMapper linksMapper;
    @Mock
    private DataMapper dataMapper;
    @Mock
    private ChildListMapper<FilingHistoryAnnotation> childListMapper;

    @Mock
    private List<FilingHistoryAnnotation> annotationList;

    @Test
    void shouldMapAnnotationToNewDocumentWhenTopLevelAnnotation() {
        // given
        Links requestLinks = new Links()
                .self("self link");
        ExternalData externalData = new ExternalData()
                .links(requestLinks)
                .date(DATE)
                .barcode("barcode")
                .paperFiled(true);
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .documentId(DOCUMENT_ID)
                        .deltaAt(DELTA_AT)
                        .matchedDefault(MATCHED_DEFAULT)
                        .originalDescription(ORIGINAL_DESCRIPTION)
                        .companyNumber(COMPANY_NUMBER)
                        .updatedBy(UPDATED_BY))
                .externalData(externalData);

        FilingHistoryLinks expectedLinks = new FilingHistoryLinks()
                .self("self link");
        FilingHistoryData expectedData = new FilingHistoryData()
                .links(expectedLinks)
                .paperFiled(true)
                .date(Instant.parse(DATE));
        FilingHistoryDeltaTimestamp expectedTimestamp = new FilingHistoryDeltaTimestamp()
                .at(UPDATED_AT)
                .by(UPDATED_BY);
        FilingHistoryDocument expected = new FilingHistoryDocument()
                .transactionId(TRANSACTION_ID)
                .data(expectedData)
                .entityId(ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .documentId(DOCUMENT_ID)
                .deltaAt(DELTA_AT)
                .updated(expectedTimestamp)
                .created(expectedTimestamp)
                .barcode("barcode")
                .originalDescription("original description")
                .matchedDefault(1);

        when(linksMapper.map(any())).thenReturn(expectedLinks);
        when(dataMapper.map(any(), any())).thenReturn(expectedData);

        // when
        FilingHistoryDocument actual = annotationTransactionMapper.mapNewFilingHistory(TRANSACTION_ID, request,
                UPDATED_AT);

        // then
        assertEquals(expected, actual);
        verify(linksMapper).map(requestLinks);
        verify(dataMapper).map(externalData, new FilingHistoryData());
        verify(childListMapper).mapChildList(eq(request), isNull(), any());
    }

    @Test
    void shouldMapAnnotationToNewDocumentWhenChildAnnotation() {
        // given
        Links requestLinks = new Links()
                .self("self link");
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .parentEntityId(PARENT_ENTITY_ID)
                        .documentId(DOCUMENT_ID)
                        .deltaAt(DELTA_AT)
                        .matchedDefault(MATCHED_DEFAULT)
                        .originalDescription(ORIGINAL_DESCRIPTION)
                        .companyNumber(COMPANY_NUMBER)
                        .updatedBy(UPDATED_BY))
                .externalData(new ExternalData()
                        .links(requestLinks)
                        .paperFiled(true));

        FilingHistoryLinks expectedLinks = new FilingHistoryLinks()
                .self("self link");
        FilingHistoryDeltaTimestamp expectedTimestamp = new FilingHistoryDeltaTimestamp()
                .at(UPDATED_AT)
                .by(UPDATED_BY);
        FilingHistoryDocument expected = new FilingHistoryDocument()
                .transactionId(TRANSACTION_ID)
                .data(new FilingHistoryData()
                        .links(expectedLinks)
                        .paperFiled(true))
                .entityId(PARENT_ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .updated(expectedTimestamp)
                .created(expectedTimestamp);

        when(linksMapper.map(any())).thenReturn(expectedLinks);

        // when
        FilingHistoryDocument actual = annotationTransactionMapper.mapNewFilingHistory(TRANSACTION_ID, request,
                UPDATED_AT);

        // then
        assertEquals(expected, actual);
        verify(linksMapper).map(requestLinks);
        verifyNoInteractions(dataMapper);
        verify(childListMapper).mapChildList(eq(request), isNull(), any());
    }

    @Test
    void shouldMapAnnotationToExistingDocumentWhenTopLevelAnnotation() {
        // given
        ExternalData externalData = new ExternalData()
                .barcode(BARCODE);
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .documentId(DOCUMENT_ID)
                        .deltaAt(DELTA_AT)
                        .matchedDefault(MATCHED_DEFAULT)
                        .originalDescription(ORIGINAL_DESCRIPTION)
                        .companyNumber(COMPANY_NUMBER)
                        .updatedBy(UPDATED_BY))
                .externalData(externalData);

        FilingHistoryDeltaTimestamp existingTimestamp = new FilingHistoryDeltaTimestamp()
                .at(CREATED_AT)
                .by(CREATED_BY);
        FilingHistoryLinks existingLinks = new FilingHistoryLinks()
                .self("self link")
                .documentMetadata("metadata");
        FilingHistoryData existingData = new FilingHistoryData()
                .links(existingLinks)
                .paperFiled(true)
                .annotations(annotationList);
        FilingHistoryDocument existingDocument = new FilingHistoryDocument()
                .data(existingData)
                .created(existingTimestamp)
                .updated(existingTimestamp);

        FilingHistoryData mappedData = new FilingHistoryData()
                .category("annotation")
                .paperFiled(true)
                .annotations(annotationList);

        FilingHistoryData expectedData = new FilingHistoryData()
                .category("annotation")
                .paperFiled(true)
                .annotations(annotationList);
        FilingHistoryDocument expected = new FilingHistoryDocument()
                .entityId(ENTITY_ID)
                .documentId(DOCUMENT_ID)
                .deltaAt(DELTA_AT)
                .matchedDefault(MATCHED_DEFAULT)
                .originalDescription(ORIGINAL_DESCRIPTION)
                .companyNumber(COMPANY_NUMBER)
                .updated(new FilingHistoryDeltaTimestamp()
                        .by(UPDATED_BY)
                        .at(UPDATED_AT))
                .created(existingTimestamp)
                .barcode(BARCODE)
                .data(expectedData);

        when(dataMapper.map(any(), any())).thenReturn(mappedData);

        // when
        FilingHistoryDocument actual = annotationTransactionMapper.mapExistingFilingHistory(
                request, existingDocument, UPDATED_AT);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(linksMapper);
        verify(dataMapper).map(externalData, existingData);
        verify(childListMapper).mapChildList(eq(request), eq(annotationList), any());
    }

    @Test
    void shouldMapAnnotationToExistingDocumentWhenChildAnnotation() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .parentEntityId(PARENT_ENTITY_ID)
                        .documentId(DOCUMENT_ID)
                        .deltaAt(DELTA_AT)
                        .matchedDefault(MATCHED_DEFAULT)
                        .originalDescription(ORIGINAL_DESCRIPTION)
                        .companyNumber(COMPANY_NUMBER)
                        .updatedBy(UPDATED_BY))
                .externalData(new ExternalData()
                        .paperFiled(true)
                        .barcode(BARCODE));

        FilingHistoryDeltaTimestamp existingTimestamp = new FilingHistoryDeltaTimestamp()
                .at(CREATED_AT)
                .by(CREATED_BY);
        FilingHistoryLinks existingLinks = new FilingHistoryLinks()
                .self("self link")
                .documentMetadata("metadata");
        FilingHistoryData existingData = new FilingHistoryData()
                .links(existingLinks)
                .annotations(annotationList);
        FilingHistoryDocument existingDocument = new FilingHistoryDocument()
                .data(existingData)
                .created(existingTimestamp)
                .updated(existingTimestamp);

        FilingHistoryDocument expected = new FilingHistoryDocument()
                .entityId(PARENT_ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .updated(new FilingHistoryDeltaTimestamp()
                        .by(UPDATED_BY)
                        .at(UPDATED_AT))
                .data(new FilingHistoryData()
                        .links(existingLinks)
                        .paperFiled(true)
                        .annotations(annotationList))
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(UPDATED_AT)
                        .by(UPDATED_BY))
                .created(existingTimestamp);

        // when
        FilingHistoryDocument actual = annotationTransactionMapper.mapExistingFilingHistory(
                request, existingDocument, UPDATED_AT);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(linksMapper);
        verifyNoInteractions(dataMapper);
        verify(childListMapper).mapChildList(eq(request), eq(annotationList), any());
    }
}
