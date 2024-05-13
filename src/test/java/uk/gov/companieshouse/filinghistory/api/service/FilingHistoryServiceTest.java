package uk.gov.companieshouse.filinghistory.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeleteAggregate;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryListAggregate;
import uk.gov.companieshouse.filinghistory.api.repository.Repository;

@ExtendWith(MockitoExtension.class)
class FilingHistoryServiceTest {

    public static final int DEFAULT_ITEMS_PER_PAGE = 25;
    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";
    public static final int START_INDEX = 0;

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
    void findCompanyFilingHistoryListShouldCallRepositoryWithCorrectCategories(List<String> actualCategories,
            List<String> expectedCategories) {
        // given
        when(repository.findCompanyFilingHistory(any(), anyInt(), anyInt(), any()))
                .thenReturn(filingHistoryListAggregate);
        when(filingHistoryListAggregate.getTotalCount()).thenReturn(1);

        // when
        final Optional<FilingHistoryListAggregate> actualFilingHistoryListAggregate = service
                .findCompanyFilingHistoryList(COMPANY_NUMBER, START_INDEX, DEFAULT_ITEMS_PER_PAGE, actualCategories);

        // then
        assertTrue(actualFilingHistoryListAggregate.isPresent());
        verify(repository).findCompanyFilingHistory(COMPANY_NUMBER, START_INDEX, DEFAULT_ITEMS_PER_PAGE,
                expectedCategories);

    }

    @Test
    void findCompanyFilingHistoryListShouldCallRepositoryAndReturnEmptyWhenTotalCountZero() {
        // given
        when(repository.findCompanyFilingHistory(any(), anyInt(), anyInt(), any()))
                .thenReturn(filingHistoryListAggregate);
        when(filingHistoryListAggregate.getTotalCount()).thenReturn(0);

        // when
        final Optional<FilingHistoryListAggregate> actualFilingHistoryListAggregate = service
                .findCompanyFilingHistoryList(COMPANY_NUMBER, START_INDEX, DEFAULT_ITEMS_PER_PAGE, List.of());

        // then
        assertTrue(actualFilingHistoryListAggregate.isEmpty());
        verify(repository).findCompanyFilingHistory(COMPANY_NUMBER, START_INDEX, DEFAULT_ITEMS_PER_PAGE, List.of());
    }

    @Test
    void findCompanyFilingHistoryThrowsServiceUnavailableException() {
        // given
        when(repository.findCompanyFilingHistory(any(), anyInt(), anyInt(), any())).thenThrow(ServiceUnavailableException.class);

        // when
        Executable executable = () -> service.findCompanyFilingHistoryList(COMPANY_NUMBER, START_INDEX,
                DEFAULT_ITEMS_PER_PAGE, List.of());

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(repository).findCompanyFilingHistory(COMPANY_NUMBER, START_INDEX, DEFAULT_ITEMS_PER_PAGE, List.of());
    }

    @Test
    void insertFilingHistorySavesDocumentAndCallsKafkaApiThenReturnsUpsertSuccessful() {
        // given
        when(resourceChangedApiClient.callResourceChanged(any())).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);

        // when
        service.insertFilingHistory(document);

        // then
        verify(repository).save(document);
        verify(resourceChangedApiClient).callResourceChanged(any());
    }

    @Test
    void updateFilingHistorySavesDocumentAndCallsKafkaApiThenReturnsUpsertSuccessful() {
        // given
        when(resourceChangedApiClient.callResourceChanged(any())).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);

        // when
        service.updateFilingHistory(document, existingDocument);

        // then
        verify(repository).save(document);
        verify(resourceChangedApiClient).callResourceChanged(any());
    }

    @Test
    void updateFilingHistorySavesDocumentButResourceChangedCallReturnsNon200() {
        // given
        when(resourceChangedApiClient.callResourceChanged(any())).thenReturn(response);
        when(response.getStatusCode()).thenReturn(503);

        // when
        Executable executable = () -> service.updateFilingHistory(document, existingDocument);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(repository).save(document);
        verify(resourceChangedApiClient).callResourceChanged(any());
        verify(repository).save(existingDocument);
    }

    @Test
    void deleteInsertedFilingHistoryDocumentWhenResourceChangedCallReturnsNon200AndOriginalDocumentCopyIsNull() {
        // given
        when(resourceChangedApiClient.callResourceChanged(any())).thenReturn(response);
        when(response.getStatusCode()).thenReturn(503);
        when(document.getTransactionId()).thenReturn(TRANSACTION_ID);

        // when
        Executable executable = () -> service.insertFilingHistory(document);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(repository).save(document);
        verify(resourceChangedApiClient).callResourceChanged(any());
        verify(repository).deleteById(TRANSACTION_ID);
    }

    @Test
    void findExistingFilingHistoryDocumentShouldThrowServiceUnavailableExceptionWhenCatchingServiceUnavailableException() {
        // given
        when(repository.findByIdAndCompanyNumber(any(), any())).thenThrow(ServiceUnavailableException.class);

        // when
        Executable executable = () -> service.findExistingFilingHistory(TRANSACTION_ID, COMPANY_NUMBER);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(repository).findByIdAndCompanyNumber(TRANSACTION_ID, COMPANY_NUMBER);
    }

    @Test
    void upsertExistingFilingHistoryDocumentShouldReturnServiceUnavailableResultWhenCatchingServiceUnavailableException() {
        // given
        doThrow(ServiceUnavailableException.class)
                .when(repository).save(any());

        // when
        Executable executable = () -> service.insertFilingHistory(document);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(repository).save(document);
        verifyNoInteractions(resourceChangedApiClient);
    }

    @Test
    void deleteExistingFilingHistoryDocumentDeletesDocumentAndCallsChsKafkaApiReturningSuccessful() {
        //given
        when(resourceChangedApiClient.callResourceChanged(any())).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);
        when(existingDocument.getTransactionId()).thenReturn(TRANSACTION_ID);

        // when
        service.deleteExistingFilingHistory(existingDocument);

        // then
        verify(repository).deleteById(TRANSACTION_ID);
        verify(resourceChangedApiClient).callResourceChanged(any());
    }

    @Test
    void deleteFilingHistoryDeletesDocumentButResourceChangedCallReturnsNon200() {
        // given
        when(resourceChangedApiClient.callResourceChanged(any())).thenReturn(response);
        when(response.getStatusCode()).thenReturn(503);
        when(existingDocument.getTransactionId()).thenReturn(TRANSACTION_ID);

        // when
        Executable executable = () -> service.deleteExistingFilingHistory(existingDocument);

        // then
        assertThrows(ServiceUnavailableException.class, executable);
        verify(resourceChangedApiClient).callResourceChanged(any());
    }

    @Test
    void findFilingHistoryByEntityIdShouldReturnDocument() {
        // given
        when(repository.findByEntityId(any())).thenReturn(Optional.of(deleteAggregate));

        // when
        final Optional<FilingHistoryDeleteAggregate> actualDocument = service.findFilingHistoryByEntityId(TRANSACTION_ID);

        // then
        assertTrue(actualDocument.isPresent());
        verify(repository).findByEntityId(TRANSACTION_ID);
    }

    @Test
    void findFilingHistoryByEntityIdShouldReturnEmptyWhenNoDocumentExists() {
        // given
        when(repository.findByEntityId(any())).thenReturn(Optional.empty());

        // when
        final Optional<FilingHistoryDeleteAggregate> actualDocument = service.findFilingHistoryByEntityId(TRANSACTION_ID);

        // then
        assertTrue(actualDocument.isEmpty());
        verify(repository).findByEntityId(TRANSACTION_ID);
    }

    private static Stream<Object> categoriesListCases() {
        return Stream.of(
                Arguments.of(null, List.of()),
                Arguments.of(List.of(), List.of()),
                Arguments.of(Stream.of("confirmation-statement").collect(Collectors.toList()),
                        List.of("confirmation-statement", "annual-return")),
                Arguments.of(Stream.of("incorporation").collect(Collectors.toList()),
                        List.of("incorporation", "change-of-constitution", "change-of-name", "court-order",
                                "gazette", "reregistration", "resolution", "restoration")),
                Arguments.of(Stream.of("gibberish").collect(Collectors.toList()), List.of("gibberish")),
                Arguments.of(Stream.of("confirmation-statement", "incorporation").collect(Collectors.toList()),
                        List.of("confirmation-statement", "incorporation", "annual-return", "change-of-constitution",
                                "change-of-name", "court-order", "gazette", "reregistration", "resolution",
                                "restoration"))
        );
    }
}
