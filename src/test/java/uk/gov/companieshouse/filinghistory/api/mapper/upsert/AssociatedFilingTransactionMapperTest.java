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
import java.util.ArrayList;
import java.util.List;
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
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAssociatedFiling;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@ExtendWith(MockitoExtension.class)
class AssociatedFilingTransactionMapperTest {

    private static final String ENTITY_ID = "1234567890";
    private static final String PARENT_ENTITY_ID = "0987654321";
    private static final String COMPANY_NUMBER = "123456789";
    private static final String EXISTING_DOCUMENT_DELTA_AT = "20140916230459600643";
    private static final String NEWEST_REQUEST_DELTA_AT = "20151025185208001000";
    private static final String STALE_REQUEST_DELTA_AT = "20131025185208001000";
    private static final Instant INSTANT = Instant.now();
    private static final String UPDATED_BY = "84746291";

    @InjectMocks
    private AssociatedFilingTransactionMapper associatedFilingTransactionMapper;

    @Mock
    private AssociatedFilingChildMapper associatedFilingChildMapper;
    @Mock
    private List<FilingHistoryAssociatedFiling> associatedFilingList;
    @Mock
    private FilingHistoryAssociatedFiling associatedFiling;
    @Mock
    private InternalFilingHistoryApi mockRequest;

    @Test
    void shouldAddNewAssociatedFilingToNewAssociatedFilingList() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID))
                .externalData(new ExternalData()
                        .paperFiled(true));

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData());

        when(associatedFilingChildMapper.mapChild(any(), any())).thenReturn(associatedFiling);

        // when
        associatedFilingTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, document, INSTANT);

        // then
        verify(associatedFilingChildMapper).mapChild(new FilingHistoryAssociatedFiling(), request);
        verifyNoMoreInteractions(associatedFilingChildMapper);
    }

    @Test
    void shouldAddNewAssociatedFilingToExistingAssociatedFilingList() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID))
                .externalData(new ExternalData()
                        .paperFiled(true));

        associatedFilingList.add(associatedFiling);
        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .associatedFilings(associatedFilingList));

        // when
        associatedFilingTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, document, INSTANT);

        // then
        verify(associatedFilingChildMapper).mapChild(new FilingHistoryAssociatedFiling(), request);
        verifyNoMoreInteractions(associatedFilingChildMapper);
    }

    @Test
    void shouldUpdateAssociatedFilingInExistingAssociatedFilingList() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT))
                .externalData(new ExternalData()
                        .paperFiled(true));

        FilingHistoryAssociatedFiling associatedFilingWithEntityIdMatch = new FilingHistoryAssociatedFiling()
                .entityId(ENTITY_ID)
                .deltaAt(EXISTING_DOCUMENT_DELTA_AT);

        FilingHistoryAssociatedFiling associatedFilingWithEntityNoIdMatch = new FilingHistoryAssociatedFiling()
                .entityId("1111111111")
                .deltaAt(EXISTING_DOCUMENT_DELTA_AT);

        List<FilingHistoryAssociatedFiling> list = List.of(
                associatedFilingWithEntityNoIdMatch,
                associatedFilingWithEntityIdMatch
        );

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .associatedFilings(list));

        // when
        associatedFilingTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, document, INSTANT);

        // then
        verify(associatedFilingChildMapper).mapChild(associatedFilingWithEntityIdMatch, request);
        verifyNoMoreInteractions(associatedFilingChildMapper);
    }

    @Test
    void shouldReturnFilingHistoryDocumentWhenMappingAssociatedFiling() {
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

        FilingHistoryAssociatedFiling associatedFiling = new FilingHistoryAssociatedFiling()
                .entityId(ENTITY_ID)
                .deltaAt(EXISTING_DOCUMENT_DELTA_AT);

        List<FilingHistoryAssociatedFiling> list = List.of(
                associatedFiling
        );

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .associatedFilings(list));

        final FilingHistoryDocument expected = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .associatedFilings(list)
                        .paperFiled(true))
                .entityId(PARENT_ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(INSTANT)
                        .by(UPDATED_BY));

        // when
        final FilingHistoryDocument actual = associatedFilingTransactionMapper.mapTopLevelFields(request, document,
                INSTANT);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldMapFilingHistoryDataWhenNewAssociatedFilingWithNoParent() {
        // given
        final FilingHistoryData expected = new FilingHistoryData()
                .associatedFilings(List.of(associatedFiling));

        when(associatedFilingChildMapper.mapChild(any(), any())).thenReturn(associatedFiling);

        // when
        final FilingHistoryData actual = associatedFilingTransactionMapper.mapFilingHistoryData(mockRequest, new FilingHistoryData());

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

        FilingHistoryAssociatedFiling associatedFiling = new FilingHistoryAssociatedFiling()
                .entityId(ENTITY_ID)
                .deltaAt(EXISTING_DOCUMENT_DELTA_AT);

        List<FilingHistoryAssociatedFiling> list = List.of(
                associatedFiling
        );

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .associatedFilings(list));
        // when
        Executable executable = () -> associatedFilingTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, document,
                INSTANT);

        // then
        assertThrows(ConflictException.class, executable);

        // Assert existing associated filing was not updated
        assertEquals(EXISTING_DOCUMENT_DELTA_AT, associatedFiling.getDeltaAt());
        verifyNoInteractions(associatedFilingChildMapper);
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

        FilingHistoryAssociatedFiling associatedFiling = new FilingHistoryAssociatedFiling()
                .entityId(ENTITY_ID)
                .deltaAt(existingDeltaAt);

        List<FilingHistoryAssociatedFiling> list = List.of(
                associatedFiling
        );

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .associatedFilings(list));
        // when
        Executable executable = () -> associatedFilingTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, document,
                INSTANT);

        // then
        assertDoesNotThrow(executable);
    }

        @Test
    void shouldAddNewAssociatedFilingToExistingAssociatedFilingListWhenAChildIsMissingAnEntityId() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID))
                .externalData(new ExternalData()
                        .paperFiled(true));

        List<FilingHistoryAssociatedFiling> associatedFilings = new ArrayList<>();
        associatedFilings.add(associatedFiling);
        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .associatedFilings(associatedFilings));

        when(associatedFiling.getEntityId()).thenReturn(null);

        // when
        associatedFilingTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, document, INSTANT);

        // then
        verify(associatedFilingChildMapper).mapChild(new FilingHistoryAssociatedFiling(), request);
        verifyNoMoreInteractions(associatedFilingChildMapper);
    }
}
