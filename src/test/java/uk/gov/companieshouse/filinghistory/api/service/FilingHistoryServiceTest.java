package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.filinghistory.api.client.ResourceChangedApiClient;
import uk.gov.companieshouse.filinghistory.api.exception.BadGatewayException;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeleteAggregate;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryIds;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryListAggregate;
import uk.gov.companieshouse.filinghistory.api.repository.Repository;

@ExtendWith(MockitoExtension.class)
class FilingHistoryServiceTest {

    private static final int DEFAULT_ITEMS_PER_PAGE = 25;
    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";
    private static final int START_INDEX = 0;

    @InjectMocks
    private FilingHistoryService service;

    @Mock
    private ResourceChangedApiClient resourceChangedApiClient;
    @Mock
    private Repository repository;
    @Mock
    private FilingHistoryDocument document;
    @Mock
    private FilingHistoryListAggregate filingHistoryListAggregate;
    @Mock
    private FilingHistoryDocument existingDocument;
    @Mock
    private ApiResponse<Void> response;
    @Mock
    private FilingHistoryDeleteAggregate deleteAggregate;


    @Test
    void findExistingFilingHistoryDocumentShouldReturnDocument() {
        // given
        when(repository.findByIdAndCompanyNumber(any(), any())).thenReturn(Optional.of(document));

        // when
        final Optional<FilingHistoryDocument> actualDocument = service.findExistingFilingHistory(TRANSACTION_ID,
                COMPANY_NUMBER);

        // then
        assertTrue(actualDocument.isPresent());
        verify(repository).findByIdAndCompanyNumber(TRANSACTION_ID, COMPANY_NUMBER);
    }

    @Test
    void findExistingFilingHistoryDocumentShouldReturnEmptyWhenNoDocumentExists() {
        // given
        when(repository.findByIdAndCompanyNumber(any(), any())).thenReturn(Optional.empty());

        // when
        final Optional<FilingHistoryDocument> actualDocument = service.findExistingFilingHistory(TRANSACTION_ID,
                COMPANY_NUMBER);

        // then
        assertTrue(actualDocument.isEmpty());
        verify(repository).findByIdAndCompanyNumber(TRANSACTION_ID, COMPANY_NUMBER);
    }

    @ParameterizedTest
    @MethodSource("categoriesListCases")
    void findCompanyFilingHistoryListQueriesShouldCallRepositoryWithCorrectCategories(List<String> actualCategories,
            List<String> expectedCategories) {
        // given
        when(repository.findCompanyFilingHistoryIds(any(), anyInt(), anyInt(), any())).thenReturn(
                new FilingHistoryIds());
        when(repository.findFullFilingHistoryDocuments(any())).thenReturn(new ArrayList<>());
        when(repository.countTotal(any(), any())).thenReturn(1L);

        // when
        final Optional<FilingHistoryListAggregate> actualFilingHistoryListAggregate = service
                .findCompanyFilingHistoryList(COMPANY_NUMBER, START_INDEX, DEFAULT_ITEMS_PER_PAGE, actualCategories);

        // then
        assertTrue(actualFilingHistoryListAggregate.isPresent());
        assertEquals(1, actualFilingHistoryListAggregate.get().getTotalCount());
        verify(repository).findCompanyFilingHistoryIds(COMPANY_NUMBER, START_INDEX, DEFAULT_ITEMS_PER_PAGE,
                expectedCategories);

    }

    @Test
    void findCompanyFilingHistoryListQueriesShouldCallRepositoryAndReturnEmptyWhenTotalCountZero() {
        // given
        when(repository.findCompanyFilingHistoryIds(any(), anyInt(), anyInt(), any())).thenReturn(
                new FilingHistoryIds());
        when(repository.findFullFilingHistoryDocuments(any())).thenReturn(new ArrayList<>());
        when(repository.countTotal(any(), any())).thenReturn(0L);

        // when
        final Optional<FilingHistoryListAggregate> actualFilingHistoryListAggregate = service
                .findCompanyFilingHistoryList(COMPANY_NUMBER, START_INDEX, DEFAULT_ITEMS_PER_PAGE, List.of());

        // then
        assertTrue(actualFilingHistoryListAggregate.isEmpty());
        verify(repository).findCompanyFilingHistoryIds(COMPANY_NUMBER, START_INDEX, DEFAULT_ITEMS_PER_PAGE, List.of());
    }

    @Test
    void findCompanyFilingHistoryThrowsBadGatewayException() {
        // given
        when(repository.findCompanyFilingHistoryIds(any(), anyInt(), anyInt(), any())).thenThrow(
                BadGatewayException.class);

        // when
        Executable executable = () -> service.findCompanyFilingHistoryList(COMPANY_NUMBER, START_INDEX,
                DEFAULT_ITEMS_PER_PAGE, List.of());

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(repository).findCompanyFilingHistoryIds(COMPANY_NUMBER, START_INDEX, DEFAULT_ITEMS_PER_PAGE, List.of());
    }

    @Test
    void insertFilingHistoryInsertsDocumentAndCallsKafkaApiThenReturnsUpsertSuccessful() {
        // given
        when(resourceChangedApiClient.callResourceChanged(any())).thenReturn(response);

        // when
        service.insertFilingHistory(document, COMPANY_NUMBER, TRANSACTION_ID);

        // then
        verify(repository).insert(document);
        verify(resourceChangedApiClient).callResourceChanged(any());
    }

    @Test
    void updateFilingHistoryUpdatesDocumentAndCallsKafkaApiThenReturnsUpsertSuccessful() {
        // given
        when(resourceChangedApiClient.callResourceChanged(any())).thenReturn(response);

        // when
        service.updateFilingHistory(document, COMPANY_NUMBER, TRANSACTION_ID);

        // then
        verify(repository).update(document);
        verify(resourceChangedApiClient).callResourceChanged(any());
    }

    @Test
    void updateFilingHistoryUpdatesDocumentButResourceChangedCallReturnsNon200() {
        // given
        when(resourceChangedApiClient.callResourceChanged(any())).thenThrow(BadGatewayException.class);

        // when
        Executable executable = () -> service.updateFilingHistory(document, COMPANY_NUMBER, TRANSACTION_ID);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(repository).update(document);
        verify(resourceChangedApiClient).callResourceChanged(any());
    }

    @Test
    void updateDocumentMetadataUpdatesDocumentAndCallsKafkaApiThenReturnsUpsertSuccessful() {
        // given
        when(resourceChangedApiClient.callResourceChanged(any())).thenReturn(response);

        // when
        service.updateDocumentMetadata(document, COMPANY_NUMBER, TRANSACTION_ID);

        // then
        verify(repository).update(document);
        verify(resourceChangedApiClient).callResourceChanged(any());
    }

    @Test
    void updateDocumentMetadataUpdatesDocumentButResourceChangedCallReturnsNon200() {
        // given
        when(resourceChangedApiClient.callResourceChanged(any())).thenThrow(BadGatewayException.class);

        // when
        Executable executable = () -> service.updateDocumentMetadata(document, COMPANY_NUMBER, TRANSACTION_ID);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(repository).update(document);
        verify(resourceChangedApiClient).callResourceChanged(any());
    }

    @Test
    void findExistingFilingHistoryDocumentShouldThrowBadGatewayExceptionWhenCatchingBadGatewayException() {
        // given
        when(repository.findByIdAndCompanyNumber(any(), any())).thenThrow(BadGatewayException.class);

        // when
        Executable executable = () -> service.findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(repository).findByIdAndCompanyNumber(TRANSACTION_ID, COMPANY_NUMBER);
    }

    @Test
    void upsertExistingFilingHistoryDocumentShouldReturnBadGatewayResultWhenCatchingBadGatewayException() {
        // given
        doThrow(BadGatewayException.class)
                .when(repository).insert(any());

        // when
        Executable executable = () -> service.insertFilingHistory(document, COMPANY_NUMBER, TRANSACTION_ID);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(repository).insert(document);
        verifyNoInteractions(resourceChangedApiClient);
    }

    @Test
    void deleteExistingFilingHistoryDocumentDeletesDocumentAndCallsChsKafkaApiReturningSuccessful() {
        //given
        when(resourceChangedApiClient.callResourceChanged(any())).thenReturn(response);
        when(existingDocument.getTransactionId()).thenReturn(TRANSACTION_ID);

        // when
        service.deleteExistingFilingHistory(existingDocument, COMPANY_NUMBER, TRANSACTION_ID);

        // then
        verify(repository).deleteById(TRANSACTION_ID);
        verify(resourceChangedApiClient).callResourceChanged(any());
    }

    @Test
    void deleteWhenDocumentAlreadyDeletedCallsResourceChanged() {
        //given
        when(resourceChangedApiClient.callResourceChanged(any())).thenReturn(response);

        // when
        service.callResourceChangedAbsentParent(COMPANY_NUMBER, TRANSACTION_ID);

        // then
        verify(resourceChangedApiClient).callResourceChanged(any());
    }

    @Test
    void deleteFilingHistoryDeletesDocumentButResourceChangedCallReturnsNon200() {
        // given
        when(resourceChangedApiClient.callResourceChanged(any())).thenThrow(BadGatewayException.class);
        when(existingDocument.getTransactionId()).thenReturn(TRANSACTION_ID);

        // when
        Executable executable = () -> service.deleteExistingFilingHistory(existingDocument,
                COMPANY_NUMBER, TRANSACTION_ID);

        // then
        assertThrows(BadGatewayException.class, executable);
        verify(resourceChangedApiClient).callResourceChanged(any());
    }

    private static Stream<Object> categoriesListCases() {
        return Stream.of(
                Arguments.of(null, List.of()),
                Arguments.of(List.of(), List.of()),
                Arguments.of(
                        Stream.of("confirmation-statement").toList(),
                        List.of("confirmation-statement", "annual-return")),
                Arguments.of(
                        Stream.of("incorporation").toList(),
                        List.of("incorporation", "change-of-constitution", "change-of-name", "court-order",
                                "gazette", "reregistration", "resolution", "restoration")),
                Arguments.of(
                        Stream.of("gibberish").toList(),
                        List.of("gibberish")),
                Arguments.of(
                        Stream.of("confirmation-statement", "incorporation").toList(),
                        List.of("confirmation-statement", "incorporation", "annual-return", "change-of-constitution",
                                "change-of-name", "court-order", "gazette", "reregistration", "resolution",
                                "restoration"))
        );
    }
}
