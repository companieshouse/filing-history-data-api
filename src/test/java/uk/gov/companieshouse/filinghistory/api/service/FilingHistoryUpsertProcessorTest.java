package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.BadRequestException;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AbstractTransactionMapper;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AbstractTransactionMapperFactory;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AnnotationTransactionMapper;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.TopLevelTransactionMapper;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.serdes.FilingHistoryDocumentCopier;

@ExtendWith(MockitoExtension.class)
class FilingHistoryUpsertProcessorTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";

    @InjectMocks
    private FilingHistoryUpsertProcessor filingHistoryProcessor;

    @Mock
    private FilingHistoryService filingHistoryService;
    @Mock
    private AbstractTransactionMapperFactory mapperFactory;
    @Mock
    private TopLevelTransactionMapper topLevelMapper;
    @Mock
    private AnnotationTransactionMapper annotationTransactionMapper;
    @Mock
    private Validator<InternalFilingHistoryApi> filingHistoryPutRequestValidator;
    @Mock
    private FilingHistoryDocumentCopier filingHistoryDocumentCopier;

    @Mock
    private InternalFilingHistoryApi request;
    @Mock
    private InternalData internalData;
    @Mock
    private FilingHistoryDocument documentToUpsert;

    @Mock
    private FilingHistoryDocument existingDocument;
    @Mock
    private FilingHistoryDocument existingDocumentCopy;


    @ParameterizedTest
    @CsvSource({
            "top-level",
            "annotation"
    })
    void shouldSuccessfullyCallSaveWhenInsert(final String kind) {
        // given
        when(filingHistoryPutRequestValidator.isValid(any())).thenReturn(true);
        when(request.getInternalData()).thenReturn(internalData);

        UpsertProcessorTestArguments arguments = getMapperTestArguments(kind);
        TransactionKindEnum kindEnum = arguments.kind;
        AbstractTransactionMapper mapper = arguments.mapper;

        when(internalData.getTransactionKind()).thenReturn(kindEnum);
        when(mapperFactory.getTransactionMapper(any())).thenReturn(mapper);
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.empty());
        when(mapper.mapNewFilingHistory(anyString(), any())).thenReturn(documentToUpsert);

        // when
        filingHistoryProcessor.processFilingHistory(TRANSACTION_ID, COMPANY_NUMBER, request);

        // then
        verify(mapperFactory).getTransactionMapper(kindEnum);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verifyNoInteractions(filingHistoryDocumentCopier);
        verify(mapper).mapNewFilingHistory(TRANSACTION_ID, request);
        verifyNoMoreInteractions(mapper);
        verify(filingHistoryService).insertFilingHistory(documentToUpsert);
    }

    @ParameterizedTest
    @CsvSource({
            "top-level",
            "annotation"
    })
    void shouldSuccessfullyCallSaveWhenUpdate(final String kind) {
        // given
        when(filingHistoryPutRequestValidator.isValid(any())).thenReturn(true);
        when(request.getInternalData()).thenReturn(internalData);

        UpsertProcessorTestArguments arguments = getMapperTestArguments(kind);
        TransactionKindEnum kindEnum = arguments.kind;
        AbstractTransactionMapper mapper = arguments.mapper;

        when(internalData.getTransactionKind()).thenReturn(kindEnum);
        when(mapperFactory.getTransactionMapper(any())).thenReturn(mapper);
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.of(existingDocument));
        when(filingHistoryDocumentCopier.deepCopy(any())).thenReturn(existingDocumentCopy);
        when(mapper.mapFilingHistoryUnlessStale(any(), any(FilingHistoryDocument.class))).thenReturn(documentToUpsert);

        // when
        filingHistoryProcessor.processFilingHistory(TRANSACTION_ID, COMPANY_NUMBER, request);

        // then
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verify(filingHistoryDocumentCopier).deepCopy(existingDocument);
        verify(mapper).mapFilingHistoryUnlessStale(request, existingDocument);
        verifyNoMoreInteractions(mapper);
        verify(filingHistoryService).updateFilingHistory(documentToUpsert, existingDocumentCopy);
    }

    @ParameterizedTest
    @CsvSource({
            "top-level",
            "annotation"
    })
    void shouldSuccessfullyCallSaveWhenUpdateButStaleDeltaAt(final String kind) {
        // given
        when(filingHistoryPutRequestValidator.isValid(any())).thenReturn(true);
        when(request.getInternalData()).thenReturn(internalData);

        UpsertProcessorTestArguments arguments = getMapperTestArguments(kind);
        TransactionKindEnum kindEnum = arguments.kind;
        AbstractTransactionMapper mapper = arguments.mapper;

        when(internalData.getTransactionKind()).thenReturn(kindEnum);
        when(mapperFactory.getTransactionMapper(any())).thenReturn(mapper);
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.of(existingDocument));
        when(mapper.mapFilingHistoryUnlessStale(any(), any())).thenThrow(ConflictException.class);

        // when
        Executable executable = () -> filingHistoryProcessor.processFilingHistory(TRANSACTION_ID, COMPANY_NUMBER, request);

        // then
        assertThrows(ConflictException.class, executable);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verify(filingHistoryDocumentCopier).deepCopy(existingDocument);
        verify(mapper).mapFilingHistoryUnlessStale(request, existingDocument);
        verifyNoMoreInteractions(mapper);
        verifyNoMoreInteractions(filingHistoryService);
    }

    @ParameterizedTest
    @CsvSource({
            "top-level",
            "annotation"
    })
    void shouldThrowServiceUnavailableWhenFindingDocumentInDB(final String kind) {
        // given
        when(filingHistoryPutRequestValidator.isValid(any())).thenReturn(true);
        when(request.getInternalData()).thenReturn(internalData);

        UpsertProcessorTestArguments arguments = getMapperTestArguments(kind);
        TransactionKindEnum kindEnum = arguments.kind;
        AbstractTransactionMapper mapper = arguments.mapper;

        when(internalData.getTransactionKind()).thenReturn(kindEnum);
        when(mapperFactory.getTransactionMapper(any())).thenReturn(mapper);
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenThrow(ServiceUnavailableException.class);

        // when
        Executable executable = () -> filingHistoryProcessor.processFilingHistory(TRANSACTION_ID, COMPANY_NUMBER, request);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(mapperFactory).getTransactionMapper(kindEnum);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verifyNoInteractions(filingHistoryDocumentCopier);
        verifyNoInteractions(mapper);
        verifyNoMoreInteractions(filingHistoryService);
    }

    @Test
    void shouldThrowBadRequestWhenValidatorReturnsFalse() {
        // given
        when(filingHistoryPutRequestValidator.isValid(any())).thenReturn(false);

        // when
        Executable executable = () -> filingHistoryProcessor.processFilingHistory(TRANSACTION_ID, COMPANY_NUMBER, request);

        // then
        assertThrows(BadRequestException.class, executable);
        verifyNoInteractions(mapperFactory);
        verifyNoInteractions(filingHistoryService);
        verifyNoInteractions(filingHistoryDocumentCopier);
        verifyNoInteractions(topLevelMapper);
        verifyNoInteractions(annotationTransactionMapper);
        verifyNoInteractions(filingHistoryService);
    }

    private record UpsertProcessorTestArguments(TransactionKindEnum kind, AbstractTransactionMapper mapper) {
    }

    private UpsertProcessorTestArguments getMapperTestArguments(final String kind) {
        return switch (kind) {
            case "top-level" -> new UpsertProcessorTestArguments(TransactionKindEnum.TOP_LEVEL, topLevelMapper);
            case "annotation" -> new UpsertProcessorTestArguments(TransactionKindEnum.ANNOTATION, annotationTransactionMapper);
            default -> throw new IllegalStateException("Unexpected value: " + kind);
        };
    }
}