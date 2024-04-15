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
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@ExtendWith(MockitoExtension.class)
class ResolutionTransactionMapperTest {

    private static final String ENTITY_ID = "1234567890";
    private static final String PARENT_ENTITY_ID = "0987654321";
    private static final String COMPANY_NUMBER = "123456789";
    private static final String EXISTING_DOCUMENT_DELTA_AT = "20140916230459600643";
    private static final String NEWEST_REQUEST_DELTA_AT = "20151025185208001000";
    private static final String STALE_REQUEST_DELTA_AT = "20131025185208001000";
    private static final String UPDATED_BY = "84746291";

    @InjectMocks
    private ResolutionTransactionMapper resolutionTransactionMapper;

    @Mock
    private ResolutionChildMapper resolutionChildMapper;
    @Mock
    private TopLevelTransactionMapper topLevelTransactionMapper;
    @Mock
    private Supplier<Instant> instantSupplier;
    @Mock
    private List<FilingHistoryResolution> resolutionList;
    @Mock
    private FilingHistoryResolution resolution;
    @Mock
    private InternalFilingHistoryApi mockRequest;


    @Test
    void shouldAddNewResolutionToNewResolutionList() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID))
                .externalData(new ExternalData()
                        .paperFiled(true));

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData());

        when(resolutionChildMapper.mapChild(any(), any())).thenReturn(resolution);

        // when
        resolutionTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, document);

        // then
        verify(resolutionChildMapper).mapChild(new FilingHistoryResolution(), request);
        verifyNoMoreInteractions(resolutionChildMapper);
    }

    @Test
    void shouldAddNewResolutionToExistingResolutionList() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID))
                .externalData(new ExternalData()
                        .paperFiled(true));

        resolutionList.add(resolution);
        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .resolutions(resolutionList));

        // when
        resolutionTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, document);

        // then
        verify(resolutionChildMapper).mapChild(new FilingHistoryResolution(), request);
        verifyNoMoreInteractions(resolutionChildMapper);
    }

    @Test
    void shouldUpdateResolutionInExistingResolutionList() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT))
                .externalData(new ExternalData()
                        .paperFiled(true));

        FilingHistoryResolution resolutionWithEntityIdMatch = new FilingHistoryResolution()
                .entityId(ENTITY_ID)
                .deltaAt(EXISTING_DOCUMENT_DELTA_AT);

        FilingHistoryResolution resolutionWithEntityIdNoMatch = new FilingHistoryResolution()
                .entityId("1111111111")
                .deltaAt(EXISTING_DOCUMENT_DELTA_AT);

        List<FilingHistoryResolution> list = List.of(
                resolutionWithEntityIdNoMatch,
                resolutionWithEntityIdMatch
        );

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .resolutions(list));

        // when
        resolutionTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, document);

        // then
        verify(resolutionChildMapper).mapChild(resolutionWithEntityIdMatch, request);
        verifyNoMoreInteractions(resolutionChildMapper);
    }

    @Test
    void shouldReturnFilingHistoryDocumentWhenMappingResolution() {
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

        FilingHistoryResolution resolution = new FilingHistoryResolution()
                .entityId(ENTITY_ID)
                .deltaAt(EXISTING_DOCUMENT_DELTA_AT);

        List<FilingHistoryResolution> list = List.of(
                resolution
        );

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .resolutions(list));

        final FilingHistoryDocument expected = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .resolutions(list)
                        .paperFiled(true))
                .companyNumber(COMPANY_NUMBER)
                .updatedBy(UPDATED_BY)
                .deltaAt(NEWEST_REQUEST_DELTA_AT);

        // when
        final FilingHistoryDocument actual = resolutionTransactionMapper.mapTopLevelFields(request, document);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldMapFilingHistoryDataWhenNewResolutionWithNoParent() {
        // given
        final FilingHistoryData expected = new FilingHistoryData()
                .resolutions(List.of(resolution));

        when(resolutionChildMapper.mapChild(any(), any())).thenReturn(resolution);

        // when
        final FilingHistoryData actual = resolutionTransactionMapper.mapFilingHistoryData(mockRequest, new FilingHistoryData());

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

        FilingHistoryResolution resolution = new FilingHistoryResolution()
                .entityId(ENTITY_ID)
                .deltaAt(EXISTING_DOCUMENT_DELTA_AT);

        List<FilingHistoryResolution> list = List.of(
                resolution
        );

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .resolutions(list));
        // when
        Executable executable = () -> resolutionTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, document);

        // then
        assertThrows(ConflictException.class, executable);

        // Assert existing resolution was not updated
        assertEquals(EXISTING_DOCUMENT_DELTA_AT, resolution.getDeltaAt());
        verifyNoInteractions(resolutionChildMapper);
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

        FilingHistoryResolution resolution = new FilingHistoryResolution()
                .entityId(ENTITY_ID)
                .deltaAt(existingDeltaAt);

        List<FilingHistoryResolution> list = List.of(
                resolution
        );

        FilingHistoryDocument document = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .resolutions(list));
        // when
        Executable executable = () -> resolutionTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, document);

        // then
        assertDoesNotThrow(executable);
    }


}
