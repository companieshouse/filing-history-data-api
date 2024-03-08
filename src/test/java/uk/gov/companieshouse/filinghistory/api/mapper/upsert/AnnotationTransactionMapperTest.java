package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@ExtendWith(MockitoExtension.class)
class AnnotationTransactionMapperTest {

    private static final String ENTITY_ID = "1234567890";
    private static final String PARENT_ENTITY_ID = "0987654321";
    private static final String COMPANY_NUMBER = "123456789";
    private static final String EXISTING_DOCUMENT_DELTA_AT = "20140916230459600643";
    private static final String NEWEST_REQUEST_DELTA_AT = "20151025185208001000";
    private static final String UPDATED_BY = "84746291";

    @InjectMocks
    private AnnotationTransactionMapper annotationTransactionMapper;

    @Mock
    private AnnotationListMapper annotationListMapper;
    @Mock
    private Supplier<Instant> instantSupplier;

    @Mock
    private List<FilingHistoryAnnotation> annotationList;


    @Test
    void shouldAddNewAnnotationToNewAnnotationList() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID))
                .externalData(new ExternalData()
                        .paperFiled(true));

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData());

        // when
        annotationTransactionMapper.mapFilingHistoryUnlessStale(request, document);

        // then
        verify(annotationListMapper).addNewAnnotationToList(new ArrayList<>(), request);
        verifyNoMoreInteractions(annotationListMapper);
    }

    @Test
    void shouldAddNewAnnotationToExistingAnnotationList() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID))
                .externalData(new ExternalData()
                        .paperFiled(true));

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .annotations(annotationList));

        // when
        annotationTransactionMapper.mapFilingHistoryUnlessStale(request, document);

        // then
        verify(annotationListMapper).addNewAnnotationToList(annotationList, request);
        verifyNoMoreInteractions(annotationListMapper);
    }

    @Test
    void shouldUpdateAnnotationToExistingAnnotationList() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT))
                .externalData(new ExternalData()
                        .paperFiled(true));

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
                        .updatedBy(UPDATED_BY))
                .externalData(new ExternalData()
                        .paperFiled(true));

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
                        .annotations(list)
                        .paperFiled(true))
                .entityId(PARENT_ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .updatedBy(UPDATED_BY);

        // when
        final FilingHistoryDocument actual = annotationTransactionMapper.mapFilingHistory(request, document);

        // then
        assertEquals(expected, actual);
    }
}
