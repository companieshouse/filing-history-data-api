package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@ExtendWith(MockitoExtension.class)
class AnnotationTransactionMapperTest {

    private static final String ENTITY_ID = "entity ID";
    private static final String PARENT_ENTITY_ID = "parent entity ID";
    private static final String COMPANY_NUMBER = "company number";
    private static final String DOCUMENT_ID = "document ID";
    private static final String DELTA_AT = "delta at";
    private static final Instant INSTANT = Instant.now();
    private static final String UPDATED_BY = "updated by";
    private static final String ORIGINAL_DESCRIPTION = "original description";
    private static final int MATCHED_DEFAULT = 1;
    private static final String BARCODE = "barcode";

    @InjectMocks
    private AnnotationTransactionMapper annotationTransactionMapper;
    @Mock
    private DataMapper dataMapper;
    @Mock
    private ChildListMapper<FilingHistoryAnnotation> childListMapper;

    @Mock
    private FilingHistoryAnnotation annotation;

    @Test
    void shouldMapAnnotationsListAndAllTopLevelFieldsWhenTopLevelAnnotation() {
        // given
        ExternalData externalData = new ExternalData()
                .paperFiled(true)
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

        FilingHistoryData existingData = new FilingHistoryData()
                .annotations(List.of(annotation));
        FilingHistoryDocument existingDocument = new FilingHistoryDocument()
                .data(existingData);

        FilingHistoryData expectedData = new FilingHistoryData()
                .category("annotation")
                .paperFiled(true)
                .annotations(List.of(annotation));
        FilingHistoryDocument expected = new FilingHistoryDocument()
                .entityId(ENTITY_ID)
                .documentId(DOCUMENT_ID)
                .deltaAt(DELTA_AT)
                .matchedDefault(MATCHED_DEFAULT)
                .originalDescription(ORIGINAL_DESCRIPTION)
                .companyNumber(COMPANY_NUMBER)
                .updated(new FilingHistoryDeltaTimestamp()
                        .by(UPDATED_BY)
                        .at(INSTANT))
                .barcode(BARCODE)
                .data(expectedData);

        when(dataMapper.map(any(), any())).thenReturn(expectedData);

        // when
        FilingHistoryDocument actual = annotationTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(
                request, existingDocument, INSTANT);

        // then
        assertEquals(expected, actual);
        verify(dataMapper).map(externalData, existingData);
        verify(childListMapper).mapChildList(eq(request), eq(List.of(annotation)), any());
    }

    @Test
    void shouldMapAnnotationsListAndSomeTopLevelFieldsWhenChildAnnotation() {
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

        FilingHistoryDocument existingDocument = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .annotations(List.of(annotation)));

        FilingHistoryDocument expected = new FilingHistoryDocument()
                .entityId(PARENT_ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .updated(new FilingHistoryDeltaTimestamp()
                        .by(UPDATED_BY)
                        .at(INSTANT))
                .data(new FilingHistoryData()
                        .paperFiled(true)
                        .annotations(List.of(annotation)));

        // when
        FilingHistoryDocument actual = annotationTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(
                request, existingDocument, INSTANT);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(dataMapper);
        verify(childListMapper).mapChildList(eq(request), eq(List.of(annotation)), any());
    }
}
