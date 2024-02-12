package uk.gov.companieshouse.filinghistory.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Supplier;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAnnotations;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.api.handler.chskafka.PrivateChangedResourceHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryLinks;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryOriginalValues;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = FilingHistoryApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class FilingHistoryControllerIT {

    private static final String PUT_REQUEST_URI = "/filing-history-data-api/company/{company_number}/filing-history/{transaction_id}/internal";
    private static final String SINGLE_GET_REQUEST_URI = "/company/{company_number}/filing-history/{transaction_id}";
    private static final String FILING_HISTORY_COLLECTION = "company_filing_history";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String SELF_LINK = "/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID);
    private static final String ENTITY_ID = "1234567890";
    private static final String DOCUMENT_ID = "000X4BI89B65846";
    private static final String BARCODE = "X4BI89B6";
    private static final String NEWEST_REQUEST_DELTA_AT = "20140916230459600643";
    private static final String STALE_REQUEST_DELTA_AT = "20130615185208001000";
    private static final String EXISTING_DELTA_AT = "20140815230459600643";
    private static final Instant UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    private static final String UPDATED_BY = "5419d856b6a59f32b7684d0e";
    private static final String TM01_TYPE = "TM01";
    private static final String DATE = "2014-09-15T23:21:18.000Z";
    private static final Instant DATE_AS_INSTANT = Instant.parse(DATE);
    private static final String ORIGINAL_DESCRIPTION = "Appointment Terminated, Director john tester";
    private static final String OFFICER_NAME = "John Tester";
    private static final String RESIGNATION_DATE = "29/08/2014";
    private static final String DOCUMENT_METADATA = "/document/C1_z-KlM567zSgwJz8uN-UZ3_xnGfCljj3k7L69LxwA";
    private static final String DESCRIPTION = "termination-director-company-with-name-termination-date";
    private static final String SUBCATEGORY = "termination";
    private static final String CATEGORY = "officers";
    private static final String ACTION_AND_TERMINATION_DATE = "2014-08-29T00:00:00.000Z";
    private static final Instant ACTION_AND_TERMINATION_DATE_AS_INSTANT = Instant.parse(ACTION_AND_TERMINATION_DATE);
    private static final String CONTEXT_ID = "ABCD1234";
    private static final String RESOURCE_CHANGED_URI = "/resource-changed";

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.12");

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private Supplier<InternalApiClient> apiClientSupplier;
    @MockBean
    private Supplier<Instant> instantSupplier;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private HttpClient apiClient;
    @Mock
    private PrivateChangedResourceHandler privateChangedResourceHandler;
    @Mock
    private PrivateChangedResourcePost privateChangedResourcePost;
    @Mock
    private ApiResponse<Void> voidApiResponse;

    @BeforeAll
    static void start() {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
    }

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(FILING_HISTORY_COLLECTION);
        mongoTemplate.createCollection(FILING_HISTORY_COLLECTION);
    }

    @Test
    void shouldInsertDocumentAndReturn200OKWhenNoExistingDocumentInDB() throws Exception {
        // given
        final FilingHistoryDocument expectedDocument = getExpectedFilingHistoryDocument(null, null, null);
        final InternalFilingHistoryApi request = buildPutRequestBody(NEWEST_REQUEST_DELTA_AT);

        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        when(apiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenReturn(voidApiResponse);
        when(voidApiResponse.getStatusCode()).thenReturn(200);
        // when
        ResultActions result = mockMvc.perform(put(PUT_REQUEST_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.header().string(LOCATION, SELF_LINK));

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertNotNull(actualDocument);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        verify(apiClientSupplier).get();
        verify(internalApiClient).getHttpClient();
        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler).postChangedResource(RESOURCE_CHANGED_URI, getExpectedChangedResource());
        verify(privateChangedResourcePost).execute();
        verify(voidApiResponse).getStatusCode();
    }

    @Test
    void shouldUpdateDocumentAndReturn200OKWhenExistingDocumentInDB() throws Exception {
        // given
        final String jsonToInsert = IOUtils.resourceToString("/filing-history-document.json", StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);

        final FilingHistoryDocument expectedDocument = getExpectedFilingHistoryDocument(DOCUMENT_METADATA, 1,
                List.of(new FilingHistoryAnnotation().annotation("annotation")));
        final InternalFilingHistoryApi request = buildPutRequestBody(NEWEST_REQUEST_DELTA_AT);

        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        when(apiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateChangedResourceHandler()).thenReturn(privateChangedResourceHandler);
        when(privateChangedResourceHandler.postChangedResource(any(), any())).thenReturn(privateChangedResourcePost);
        when(privateChangedResourcePost.execute()).thenReturn(voidApiResponse);
        when(voidApiResponse.getStatusCode()).thenReturn(200);

        // when
        final ResultActions result = mockMvc.perform(put(PUT_REQUEST_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header("X-Request-Id", "ABCD1234")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.header().string(LOCATION, SELF_LINK));
        verify(privateChangedResourceHandler).postChangedResource(RESOURCE_CHANGED_URI, getExpectedChangedResource());

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertNotNull(actualDocument);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        verify(apiClientSupplier).get();
        verify(internalApiClient).getHttpClient();
        verify(internalApiClient).privateChangedResourceHandler();
        verify(privateChangedResourceHandler).postChangedResource(RESOURCE_CHANGED_URI, getExpectedChangedResource());
        verify(privateChangedResourcePost).execute();
        verify(voidApiResponse).getStatusCode();
    }

    @Test
    void shouldNotUpdateDocumentAndShouldReturn409ConflictWhenDeltaStale() throws Exception {
        // given
        final String jsonToInsert = IOUtils.resourceToString("/filing-history-document.json", StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);

        final InternalFilingHistoryApi request = buildPutRequestBody(STALE_REQUEST_DELTA_AT);

        // when
        final ResultActions result = mockMvc.perform(put(PUT_REQUEST_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(MockMvcResultMatchers.status().isConflict());
        result.andExpect(MockMvcResultMatchers.header().doesNotExist(LOCATION));

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertNotNull(actualDocument);
        assertEquals(EXISTING_DELTA_AT, actualDocument.getDeltaAt());
    }

    @Test
    void shouldGetSingleDocumentAndReturn200OKWhen() throws Exception {
        // given
        final ExternalData expectedResponseBody = new ExternalData()
                .transactionId(TRANSACTION_ID)
                .barcode(BARCODE)
                .actionDate("2014-08-29")
                .category(ExternalData.CategoryEnum.OFFICERS)
                .type(TM01_TYPE)
                .description(DESCRIPTION)
                .subcategory(ExternalData.SubcategoryEnum.TERMINATION)
                .date("2014-09-15")
                .descriptionValues(new FilingHistoryItemDataDescriptionValues()
                        .officerName("John Tester")
                        .terminationDate("2014-08-29"))
                .annotations(List.of(
                        new FilingHistoryItemDataAnnotations().annotation("annotation")))
                .links(new FilingHistoryItemDataLinks()
                        .self(SELF_LINK)
                        .documentMetadata("/document/C1_z-KlM567zSgwJz8uN-UZ3_xnGfCljj3k7L69LxwA"))
                .pages(1);;

        final String jsonToInsert = IOUtils.resourceToString("/filing-history-document.json", StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);

        // when
        ResultActions result = mockMvc.perform(get(SINGLE_GET_REQUEST_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app") // TODO: Check privileges
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final String responseBodyAsString = result.andReturn().getResponse().getContentAsString();
        ExternalData actualResponseBody = objectMapper.readValue(responseBodyAsString, ExternalData.class);

        assertEquals(expectedResponseBody, actualResponseBody);
    }

    private static InternalFilingHistoryApi buildPutRequestBody(String deltaAt) {
        return new InternalFilingHistoryApi()
                .internalData(buildInternalData(deltaAt))
                .externalData(buildExternalData());
    }

    private static InternalData buildInternalData(String deltaAt) {
        return new InternalData()
                .entityId(ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .documentId(DOCUMENT_ID)
                .deltaAt(deltaAt)
                .originalDescription(ORIGINAL_DESCRIPTION)
                .originalValues(new InternalDataOriginalValues()
                        .officerName(OFFICER_NAME)
                        .resignationDate(RESIGNATION_DATE))
                .parentEntityId("parent_entity_id")
                .updatedBy(UPDATED_BY)
                .transactionKind(TransactionKindEnum.TOP_LEVEL);
    }

    private static ExternalData buildExternalData() {
        return new ExternalData()
                .transactionId(TRANSACTION_ID)
                .barcode(BARCODE)
                .type(TM01_TYPE)
                .date(DATE)
                .category(ExternalData.CategoryEnum.OFFICERS)
                .annotations(null)
                .subcategory(ExternalData.SubcategoryEnum.TERMINATION)
                .description(DESCRIPTION)
                .descriptionValues(new FilingHistoryItemDataDescriptionValues()
                        .officerName(OFFICER_NAME)
                        .terminationDate(ACTION_AND_TERMINATION_DATE))
                .pages(1) // should not be mapped, persisted by document store sub delta
                .actionDate(ACTION_AND_TERMINATION_DATE)
                .paperFiled(true)
                .links(new FilingHistoryItemDataLinks()
                        .self(SELF_LINK));
    }

    private static FilingHistoryDocument getExpectedFilingHistoryDocument(String documentMetadata, Integer pages,
            List<FilingHistoryAnnotation> annotations) {
        return new FilingHistoryDocument()
                .transactionId(TRANSACTION_ID)
                .companyNumber(COMPANY_NUMBER)
                .data(new FilingHistoryData()
                        .actionDate(ACTION_AND_TERMINATION_DATE_AS_INSTANT)
                        .category(CATEGORY)
                        .type(TM01_TYPE)
                        .description(DESCRIPTION)
                        .subcategory(SUBCATEGORY)
                        .date(DATE_AS_INSTANT)
                        .paperFiled(true)
                        .descriptionValues(new FilingHistoryDescriptionValues()
                                .terminationDate(ACTION_AND_TERMINATION_DATE_AS_INSTANT)
                                .officerName(OFFICER_NAME))
                        .annotations(annotations)
                        .links(new FilingHistoryLinks()
                                .documentMetadata(documentMetadata)
                                .self(SELF_LINK))
                        .pages(pages))
                .barcode(BARCODE)
                .deltaAt(NEWEST_REQUEST_DELTA_AT)
                .entityId(ENTITY_ID)
                .updatedAt(UPDATED_AT)
                .updatedBy(UPDATED_BY)
                .originalValues(new FilingHistoryOriginalValues()
                        .officerName(OFFICER_NAME)
                        .resignationDate(RESIGNATION_DATE))
                .originalDescription(ORIGINAL_DESCRIPTION)
                .documentId(DOCUMENT_ID);
    }

    private static ChangedResource getExpectedChangedResource() {
        return new ChangedResource()
                .resourceUri("/company/12345678/filing-history/transactionId")
                .resourceKind("filing-history")
                .contextId(CONTEXT_ID)
                .deletedData(null)
                .event(new ChangedResourceEvent()
                        .fieldsChanged(null)
                        .publishedAt(UPDATED_AT.toString())
                        .type("changed"));
    }
}
