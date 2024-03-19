package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
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
    private static final String STALE_REQUEST_DELTA_AT = "20131025185208001000";
    private static final String UPDATED_BY = "84746291";

    @InjectMocks
    private AnnotationTransactionMapper annotationTransactionMapper;

    @Mock
    private AnnotationChildMapper annotationChildMapper;
    @Mock
    private Supplier<Instant> instantSupplier;

    @Mock
    private List<FilingHistoryAnnotation> annotationList;
    @Mock
    private FilingHistoryAnnotation annotation;
    @Mock
    private InternalFilingHistoryApi mockRequest;


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

        when(annotationChildMapper.mapChild(any(), any())).thenReturn(annotation);

        // when
        annotationTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, document);

        // then
        verify(annotationChildMapper).mapChild(new FilingHistoryAnnotation(), request);
        verifyNoMoreInteractions(annotationChildMapper);
    }

    @Test
    void shouldAddNewAnnotationToExistingAnnotationList() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID))
                .externalData(new ExternalData()
                        .paperFiled(true));

        annotationList.add(annotation);
        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .annotations(annotationList));

        // when
        annotationTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, document);

        // then
        verify(annotationChildMapper).mapChild(new FilingHistoryAnnotation(), request);
        verifyNoMoreInteractions(annotationChildMapper);
    }

    @Test
    void shouldUpdateAnnotationInExistingAnnotationList() {
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
        annotationTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, document);

        // then
        verify(annotationChildMapper).mapChild(annotationWithEntityIdMatch, request);
        verifyNoMoreInteractions(annotationChildMapper);
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
        final FilingHistoryDocument actual = annotationTransactionMapper.mapTopLevelFields(request, document);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldMapFilingHistoryDataWhenNewAnnotationWithNoParent() {
        // given
        final FilingHistoryData expected = new FilingHistoryData()
                .annotations(List.of(annotation));

        when(annotationChildMapper.mapChild(any(), any())).thenReturn(annotation);

        // when
        final FilingHistoryData actual = annotationTransactionMapper.mapFilingHistoryData(mockRequest, new FilingHistoryData());

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldThrow409ConflictWhenRequestHasStaleDeltaAt() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .deltaAt(STALE_REQUEST_DELTA_AT))
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
        // when
        Executable executable = () -> annotationTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, document);

        // then
        assertThrows(ConflictException.class, executable);

        // Assert existing annotation was not updated
        assertEquals(EXISTING_DOCUMENT_DELTA_AT, annotation.getDeltaAt());
        verifyNoInteractions(annotationChildMapper);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "''",
            "null"},
            nullValues = {"null"})
    void shouldNotThrow409ConflictWhenExistingDocHasEmptyOrNullDeltaAt(final String existingDeltaAt) {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .deltaAt(STALE_REQUEST_DELTA_AT))
                .externalData(new ExternalData()
                        .paperFiled(true));

        FilingHistoryAnnotation annotation = new FilingHistoryAnnotation()
                .entityId(ENTITY_ID)
                .deltaAt(existingDeltaAt);

        List<FilingHistoryAnnotation> list = List.of(
                annotation
        );

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .annotations(list));
        // when
        Executable executable = () -> annotationTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, document);

        // then
        assertDoesNotThrow(executable);
    }
}
