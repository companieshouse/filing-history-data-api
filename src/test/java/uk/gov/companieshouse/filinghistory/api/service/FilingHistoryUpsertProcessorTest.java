package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryDocumentMetadataUpdateApi;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.BadGatewayException;
import uk.gov.companieshouse.filinghistory.api.exception.BadRequestException;
import uk.gov.companieshouse.filinghistory.api.exception.ConflictException;
import uk.gov.companieshouse.filinghistory.api.exception.NotFoundException;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AbstractTransactionMapperFactory;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.AnnotationTransactionMapper;
import uk.gov.companieshouse.filinghistory.api.mapper.upsert.TopLevelTransactionMapper;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.serdes.FilingHistoryDocumentCopier;

@ExtendWith(MockitoExtension.class)
class FilingHistoryUpsertProcessorTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String DOCUMENT_METADATA_LINK = "/document/12345";
    private static final Instant INSTANT = Instant.now();

    @InjectMocks
    private FilingHistoryUpsertProcessor filingHistoryProcessor;

    @Mock
    private FilingHistoryService filingHistoryService;
    @Mock
    private AbstractTransactionMapperFactory mapperFactory;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private TopLevelTransactionMapper topLevelMapper;
    @Mock
    private AnnotationTransactionMapper annotationTransactionMapper;
    @Mock
    private Validator<InternalFilingHistoryApi> topLevelPutRequestValidator;
    @Mock
    private FilingHistoryDocumentCopier filingHistoryDocumentCopier;
    @Mock
    private Supplier<Instant> instantSupplier;
    @Mock
    private InternalFilingHistoryApi request;
    @Mock
    private FilingHistoryDocumentMetadataUpdateApi docMetadataRequest;
    @Mock
    private InternalData internalData;
    @Mock
    private FilingHistoryDocument documentToUpsert;

    @Mock
    private FilingHistoryDocument existingDocument;
    @Mock
    private FilingHistoryDocument existingDocumentCopy;

    @Test
    void shouldSuccessfullyCallInsert() {
        // given
        when(validatorFactory.getPutRequestValidator(any())).thenReturn(topLevelPutRequestValidator);
        when(instantSupplier.get()).thenReturn(INSTANT);
        when(topLevelPutRequestValidator.isValid(any())).thenReturn(true);
        when(request.getInternalData()).thenReturn(internalData);
        when(internalData.getTransactionKind()).thenReturn(TransactionKindEnum.TOP_LEVEL);
        when(mapperFactory.getTransactionMapper(any())).thenReturn(topLevelMapper);
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.empty());
        when(topLevelMapper.mapNewFilingHistory(anyString(), any(), any())).thenReturn(documentToUpsert);

        // when
        filingHistoryProcessor.processFilingHistory(TRANSACTION_ID, COMPANY_NUMBER, request);

        // then
        verify(mapperFactory).getTransactionMapper(TransactionKindEnum.TOP_LEVEL);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verifyNoInteractions(filingHistoryDocumentCopier);
        verify(instantSupplier).get();
        verify(topLevelMapper).mapNewFilingHistory(TRANSACTION_ID, request, INSTANT);
        verifyNoMoreInteractions(topLevelMapper);
        verify(filingHistoryService).insertFilingHistory(documentToUpsert);
    }

    @Test
    void shouldSuccessfullyCallUpdate() {
        // given
        when(instantSupplier.get()).thenReturn(INSTANT);
        when(validatorFactory.getPutRequestValidator(any())).thenReturn(topLevelPutRequestValidator);
        when(topLevelPutRequestValidator.isValid(any())).thenReturn(true);
        when(request.getInternalData()).thenReturn(internalData);
        when(internalData.getTransactionKind()).thenReturn(TransactionKindEnum.TOP_LEVEL);
        when(mapperFactory.getTransactionMapper(any())).thenReturn(topLevelMapper);
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.of(existingDocument));
        when(filingHistoryDocumentCopier.deepCopy(any())).thenReturn(existingDocumentCopy);
        when(topLevelMapper.mapExistingFilingHistory(any(), any(FilingHistoryDocument.class),
                any())).thenReturn(documentToUpsert);

        // when
        filingHistoryProcessor.processFilingHistory(TRANSACTION_ID, COMPANY_NUMBER, request);

        // then
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verify(filingHistoryDocumentCopier).deepCopy(existingDocument);
        verify(instantSupplier).get();
        verify(topLevelMapper).mapExistingFilingHistory(request, existingDocument, INSTANT);
        verifyNoMoreInteractions(topLevelMapper);
        verify(filingHistoryService).updateFilingHistory(documentToUpsert);
    }

    @Test
    void shouldThrowConflictExceptionWhenUpdateButStaleDeltaAt() {
        // given
        when(instantSupplier.get()).thenReturn(INSTANT);
        when(validatorFactory.getPutRequestValidator(any())).thenReturn(topLevelPutRequestValidator);
        when(topLevelPutRequestValidator.isValid(any())).thenReturn(true);
        when(request.getInternalData()).thenReturn(internalData);
        when(internalData.getTransactionKind()).thenReturn(TransactionKindEnum.TOP_LEVEL);
        when(mapperFactory.getTransactionMapper(any())).thenReturn(topLevelMapper);
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.of(existingDocument));
        when(topLevelMapper.mapExistingFilingHistory(any(), any(), any())).thenThrow(ConflictException.class);

        // when
        Executable executable = () -> filingHistoryProcessor.processFilingHistory(TRANSACTION_ID, COMPANY_NUMBER, request);

        // then
        assertThrows(ConflictException.class, executable);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verify(filingHistoryDocumentCopier).deepCopy(existingDocument);
        verify(instantSupplier).get();
        verify(topLevelMapper).mapExistingFilingHistory(request, existingDocument, INSTANT);
        verifyNoMoreInteractions(topLevelMapper);
        verifyNoMoreInteractions(filingHistoryService);
    }

    @Test
    void shouldThrowBadGatewayWhenFindingDocumentInDB() {
        // given
        when(instantSupplier.get()).thenReturn(INSTANT);
        when(validatorFactory.getPutRequestValidator(any())).thenReturn(topLevelPutRequestValidator);
        when(topLevelPutRequestValidator.isValid(any())).thenReturn(true);
        when(request.getInternalData()).thenReturn(internalData);
        when(internalData.getTransactionKind()).thenReturn(TransactionKindEnum.TOP_LEVEL);
        when(mapperFactory.getTransactionMapper(any())).thenReturn(topLevelMapper);
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenThrow(BadGatewayException.class);

        // when
        Executable executable = () -> filingHistoryProcessor.processFilingHistory(TRANSACTION_ID, COMPANY_NUMBER, request);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(mapperFactory).getTransactionMapper(TransactionKindEnum.TOP_LEVEL);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verifyNoInteractions(filingHistoryDocumentCopier);
        verifyNoInteractions(topLevelMapper);
        verifyNoMoreInteractions(filingHistoryService);
    }

    @Test
    void shouldThrowBadRequestWhenValidatorReturnsFalse() {
        // given
        when(validatorFactory.getPutRequestValidator(any())).thenReturn(topLevelPutRequestValidator);
        when(topLevelPutRequestValidator.isValid(any())).thenReturn(false);
        when(request.getInternalData()).thenReturn(internalData);
        when(internalData.getTransactionKind()).thenReturn(TransactionKindEnum.TOP_LEVEL);

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

    @Test
    void shouldSuccessfullyCallUpdateDocMetadata() {
        // given
        when(mapperFactory.getTransactionMapper(any())).thenReturn(topLevelMapper);
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.of(existingDocument));
        when(filingHistoryDocumentCopier.deepCopy(any())).thenReturn(existingDocumentCopy);
        when(docMetadataRequest.getDocumentMetadata()).thenReturn(DOCUMENT_METADATA_LINK);
        when(topLevelMapper.mapDocumentMetadata(any(), any(FilingHistoryDocument.class))).thenReturn(existingDocument);

        // when
        filingHistoryProcessor.processDocumentMetadata(TRANSACTION_ID, COMPANY_NUMBER, docMetadataRequest);

        // then
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verify(filingHistoryDocumentCopier).deepCopy(existingDocument);
        verify(topLevelMapper).mapDocumentMetadata(docMetadataRequest, existingDocumentCopy);
        verifyNoMoreInteractions(topLevelMapper);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateDocMetadataButMissingTransaction() {
        // given
        when(docMetadataRequest.getDocumentMetadata()).thenReturn(DOCUMENT_METADATA_LINK);
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenReturn(Optional.empty());

        // when
        Executable executable = () -> filingHistoryProcessor.processDocumentMetadata(TRANSACTION_ID, COMPANY_NUMBER, docMetadataRequest);

        // then
        assertThrows(NotFoundException.class, executable);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verifyNoMoreInteractions(filingHistoryService);
    }

    @Test
    void shouldThrowBadGatewayWhenFindingDocumentInDBForDocMetadataPatch() {
        // given
        when(docMetadataRequest.getDocumentMetadata()).thenReturn(DOCUMENT_METADATA_LINK);
        when(filingHistoryService.findExistingFilingHistory(any(), any())).thenThrow(BadGatewayException.class);

        // when
        Executable executable = () -> filingHistoryProcessor.processDocumentMetadata(TRANSACTION_ID, COMPANY_NUMBER, docMetadataRequest);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(mapperFactory).getTransactionMapper(TransactionKindEnum.TOP_LEVEL);
        verify(filingHistoryService).findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);
        verifyNoMoreInteractions(filingHistoryService);
    }

    @Test
    void shouldThrowBadRequestWhenDocumentMetadataLinkMissing() {
        // given
        when(docMetadataRequest.getDocumentMetadata()).thenReturn(null);

        // when
        Executable executable = () -> filingHistoryProcessor.processDocumentMetadata(TRANSACTION_ID, COMPANY_NUMBER, docMetadataRequest);

        // then
        assertThrows(BadRequestException.class, executable);
    }
}