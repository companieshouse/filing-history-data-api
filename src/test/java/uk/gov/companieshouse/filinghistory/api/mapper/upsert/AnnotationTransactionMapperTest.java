package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryLinks;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryOriginalValues;

@ExtendWith(MockitoExtension.class)
class AnnotationTransactionMapperTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String ENTITY_ID = "1234567890";
    private static final String PARENT_ENTITY_ID = "0987654321";
    private static final String COMPANY_NUMBER = "123456789";
    private static final String DOCUMENT_ID = "documentId";
    private static final String BARCODE = "barcode";
    private static final String ORIGINAL_DESCRIPTION = "original description";
    private static final String EXISTING_DOCUMENT_DELTA_AT = "20140916230459600643";
    private static final String NEWEST_REQUEST_DELTA_AT = "20151025185208001000";
    private static final String STALE_REQUEST_DELTA_AT = "20130615185208001000";
    private static final Instant UPDATED_AT = Instant.now();
    private static final String UPDATED_BY = "84746291";
    private static final String EXPECTED_DELTA_AT = NEWEST_REQUEST_DELTA_AT;

    @InjectMocks
    private AnnotationTransactionMapper annotationTransactionMapper;

    @Mock
    private DataMapper dataMapper;
    @Mock
    private Supplier<Instant> instantSupplier;
    @Mock
    private OriginalValuesMapper originalValuesMapper;
    @Mock
    private LinksMapper linksMapper;
    @Mock
    private AnnotationListMapper annotationListMapper;

    @Mock
    private FilingHistoryDocument mockDocument;
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
    private List<FilingHistoryAnnotation> annotationList;
    @Mock
    private FilingHistoryAnnotation mockAnnotation;

    @Mock
    private InternalDataOriginalValues requestOriginalValues;
    @Mock
    private ExternalData requestExternalData;
    @Mock
    private FilingHistoryItemDataLinks requestLinks;

    @Test
    void shouldAddNewAnnotationToNewAnnotationList() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID));

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData());

        // when
        annotationTransactionMapper.mapFilingHistoryUnlessStale(request, document);

        // then
        verify(annotationListMapper).addNewAnnotationToList(new ArrayList<>());
        verifyNoMoreInteractions(annotationListMapper);
    }

    @Test
    void shouldAddNewAnnotationToExistingAnnotationList() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID));

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .annotations(annotationList));

        // when
        annotationTransactionMapper.mapFilingHistoryUnlessStale(request, document);

        // then
        verify(annotationListMapper).addNewAnnotationToList(annotationList);
        verifyNoMoreInteractions(annotationListMapper);
    }

    @Test
    void shouldUpdateAnnotationToExistingAnnotationList() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT));

        FilingHistoryAnnotation annotationWithEntityIdMatch = new FilingHistoryAnnotation()
                .entityId(ENTITY_ID)
                .deltaAt(EXISTING_DOCUMENT_DELTA_AT);

        FilingHistoryAnnotation annotationWithEntityIdNoMatch = new FilingHistoryAnnotation()
                .entityId("1111111111")
                .deltaAt(EXISTING_DOCUMENT_DELTA_AT);

        List<FilingHistoryAnnotation> list = List.of(
                annotationWithEntityIdNoMatch,
                annotationWithEntityIdMatch
        );

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .annotations(list));

        // when
        annotationTransactionMapper.mapFilingHistoryUnlessStale(request, document);

        // then
        verify(annotationListMapper).updateExistingAnnotation(annotationWithEntityIdMatch);
        verifyNoMoreInteractions(annotationListMapper);
    }

    @Test
    void shouldReturnFilingHistoryDocumentWhenMappingAnnotation() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .companyNumber(COMPANY_NUMBER)
                        .parentEntityId(PARENT_ENTITY_ID)
                        .entityId(ENTITY_ID)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT)
                        .updatedBy(UPDATED_BY));

        FilingHistoryAnnotation annotation = new FilingHistoryAnnotation()
                .entityId(ENTITY_ID)
                .deltaAt(EXISTING_DOCUMENT_DELTA_AT);

        List<FilingHistoryAnnotation> list = List.of(
                annotation
        );

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .annotations(list));

        final FilingHistoryDocument expected = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .annotations(list))
                .entityId(PARENT_ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .updatedBy(UPDATED_BY);

        // when
        final FilingHistoryDocument actual = annotationTransactionMapper.mapFilingHistory(request, document);

        // then
        assertEquals(expected, actual);
    }

//    private InternalFilingHistoryApi buildPutRequestBody() {
//        return new InternalFilingHistoryApi()
//                .externalData(requestExternalData)
//                .internalData(buildInternalData());
//    }
//
//    private InternalData buildInternalData() {
//        return new InternalData()
//                .entityId(ENTITY_ID)
//                .companyNumber(COMPANY_NUMBER)
//                .documentId(DOCUMENT_ID)
//                .deltaAt(NEWEST_REQUEST_DELTA_AT)
//                .originalDescription("original description")
//                .originalValues(requestOriginalValues)
//                .parentEntityId("parent_entity_id")
//                .updatedBy(UPDATED_BY);
//    }
//
//    private FilingHistoryDocument getFilingHistoryDocument(FilingHistoryData data,
//                                                           FilingHistoryOriginalValues originalValues, String deltaAt) {
//        return new FilingHistoryDocument()
//                .transactionId(TRANSACTION_ID)
//                .entityId(ENTITY_ID)
//                .companyNumber(COMPANY_NUMBER)
//                .documentId(DOCUMENT_ID)
//                .barcode(BARCODE)
//                .data(data)
//                .originalDescription(ORIGINAL_DESCRIPTION)
//                .originalValues(originalValues)
//                .deltaAt(deltaAt)
//                .updatedAt(UPDATED_AT)
//                .updatedBy(UPDATED_BY);
//    }
}
