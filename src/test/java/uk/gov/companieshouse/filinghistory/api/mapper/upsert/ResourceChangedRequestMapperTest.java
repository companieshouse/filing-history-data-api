package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.filinghistory.api.exception.InternalServerErrorException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.mapper.get.ItemGetResponseMapper;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@ExtendWith(MockitoExtension.class)
class ResourceChangedRequestMapperTest {

    private static final Instant UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    private static final String FILING_HISTORY = "filing-history";
    private static final String EXPECTED_CONTEXT_ID = "35234234";
    private static final ExternalData deletedData = new ExternalData();
    private static final FilingHistoryDocument filingHistoryDocument = new FilingHistoryDocument()
            .companyNumber("12345678")
            .transactionId("ABCDE54321");
    private static final String RESOURCE_URI = "/company/12345678/filing-history/ABCDE54321";
    private static final String CHANGED_EVENT_TYPE = "changed";
    private static final String DELETED_EVENT_TYPE = "deleted";
    private static final String DOC_META_DATA_LINK_FIELD = "links.document_metadata";

    @InjectMocks
    private ResourceChangedRequestMapper mapper;

    @Mock
    private ItemGetResponseMapper itemGetResponseMapper;
    @Mock
    private Supplier<Instant> instantSupplier;
    @Mock
    private ObjectMapper nullCleaningObjectMapper;

    @BeforeEach
    void setUp() {
        DataMapHolder.initialise(EXPECTED_CONTEXT_ID);
    }

    @Test
    void testUpsertResourceChangedMapper() {
        // given
        when(instantSupplier.get()).thenReturn(UPDATED_AT);

        final ArrayList<String> fieldsChanged = new ArrayList<>();
        fieldsChanged.add(DOC_META_DATA_LINK_FIELD);

        ResourceChangedRequest upsertResourceChangedRequest = new ResourceChangedRequest(
                filingHistoryDocument, false, fieldsChanged);
        ChangedResource expectedChangedResource = new ChangedResource()
                .contextId(EXPECTED_CONTEXT_ID)
                .resourceUri(RESOURCE_URI)
                .resourceKind(FILING_HISTORY)
                .event(new ChangedResourceEvent()
                        .type(CHANGED_EVENT_TYPE)
                        .fieldsChanged(fieldsChanged)
                        .publishedAt(UPDATED_AT.atOffset(ZoneOffset.UTC)
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss"))));

        // when
        ChangedResource actual = mapper.mapChangedResource(upsertResourceChangedRequest);

        // then
        assertEquals(expectedChangedResource, actual);
    }

    @Test
    void testPatchDocMetadataResourceChangedMapper() {
        // given
        when(instantSupplier.get()).thenReturn(UPDATED_AT);



        ResourceChangedRequest upsertResourceChangedRequest = new ResourceChangedRequest(
                filingHistoryDocument, false, null);
        ChangedResource expectedChangedResource = new ChangedResource()
                .contextId(EXPECTED_CONTEXT_ID)
                .resourceUri(RESOURCE_URI)
                .resourceKind(FILING_HISTORY)
                .event(new ChangedResourceEvent()
                        .type(CHANGED_EVENT_TYPE)
                        .publishedAt(UPDATED_AT.atOffset(ZoneOffset.UTC)
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss"))));

        // when
        ChangedResource actual = mapper.mapChangedResource(upsertResourceChangedRequest);

        // then
        assertEquals(expectedChangedResource, actual);
    }

    @Test
    void testDeletedResourceChangedMapper() throws Exception {
        // given
        ObjectMapper objectMapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);

        final String deletedDataAsString = objectMapper.writeValueAsString(
                new ExternalData()
                        .transactionId("123"));
        Object deletedDataAsObject = objectMapper.readValue(deletedDataAsString, Object.class);

        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        when(itemGetResponseMapper.mapFilingHistoryItem(any())).thenReturn(deletedData);
        when(nullCleaningObjectMapper.writeValueAsString(any())).thenReturn(deletedDataAsString);
        when(nullCleaningObjectMapper.readValue(anyString(), eq(Object.class))).thenReturn(deletedDataAsObject);

        ResourceChangedRequest deleteResourceChangedRequest = new ResourceChangedRequest(filingHistoryDocument, true, null);
        ChangedResource expectedChangedResource = new ChangedResource()
                .contextId(EXPECTED_CONTEXT_ID)
                .resourceUri(RESOURCE_URI)
                .resourceKind(FILING_HISTORY)
                .deletedData(deletedDataAsObject)
                .event(new ChangedResourceEvent()
                        .type(DELETED_EVENT_TYPE)
                        .publishedAt(UPDATED_AT.atOffset(ZoneOffset.UTC)
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss"))));

        // when
        ChangedResource actual = mapper.mapChangedResource(deleteResourceChangedRequest);

        // then
        assertEquals(expectedChangedResource, actual);
        verify(itemGetResponseMapper).mapFilingHistoryItem(filingHistoryDocument);
        verify(nullCleaningObjectMapper).writeValueAsString(deletedData);
        verify(nullCleaningObjectMapper).readValue(deletedDataAsString, Object.class);
    }

    @Test
    void shouldThrowInternalServerErrorForDeletedResourceChangedMapperOnSerialisation() throws Exception {
        // given
        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        when(itemGetResponseMapper.mapFilingHistoryItem(any())).thenReturn(deletedData);
        when(nullCleaningObjectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        ResourceChangedRequest deleteResourceChangedRequest = new ResourceChangedRequest(filingHistoryDocument, true, null);

        // when
        Executable executable = () -> mapper.mapChangedResource(deleteResourceChangedRequest);

        // then
        assertThrows(InternalServerErrorException.class, executable);
        verify(itemGetResponseMapper).mapFilingHistoryItem(filingHistoryDocument);
        verify(nullCleaningObjectMapper).writeValueAsString(deletedData);
        verifyNoMoreInteractions(nullCleaningObjectMapper);
    }

    @Test
    void shouldThrowInternalServerErrorForDeletedResourceChangedMapperOnDeserialisation() throws Exception {
        // given
        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        when(itemGetResponseMapper.mapFilingHistoryItem(any())).thenReturn(deletedData);
        when(nullCleaningObjectMapper.writeValueAsString(any())).thenReturn("deletedDataAsString");
        when(nullCleaningObjectMapper.readValue(anyString(), eq(Object.class))).thenThrow(JsonProcessingException.class);

        ResourceChangedRequest deleteResourceChangedRequest = new ResourceChangedRequest(filingHistoryDocument, true, null);

        // when
        Executable executable = () -> mapper.mapChangedResource(deleteResourceChangedRequest);

        // then
        assertThrows(InternalServerErrorException.class, executable);
        verify(itemGetResponseMapper).mapFilingHistoryItem(filingHistoryDocument);
        verify(nullCleaningObjectMapper).writeValueAsString(deletedData);
        verify(nullCleaningObjectMapper).readValue("deletedDataAsString", Object.class);
    }
}
