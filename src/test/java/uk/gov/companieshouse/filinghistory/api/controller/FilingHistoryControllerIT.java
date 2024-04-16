package uk.gov.companieshouse.filinghistory.api.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.requestMadeFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Supplier;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import uk.gov.companieshouse.api.filinghistory.Annotation;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryList;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryList.FilingHistoryStatusEnum;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.api.filinghistory.Links;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryLinks;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryOriginalValues;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
@WireMockTest(httpPort = 8889)
class FilingHistoryControllerIT {

    private static final String PUT_REQUEST_URI = "/filing-history-data-api/company/{company_number}/filing-history/{transaction_id}/internal";
    private static final String DELETE_REQUEST_URI = "/filing-history-data-api/filing-history/{transaction_id}/internal";
    private static final String SINGLE_GET_REQUEST_URI = "/filing-history-data-api/company/{company_number}/filing-history/{transaction_id}";
    private static final String LIST_GET_REQUEST_URI = "/filing-history-data-api/company/{company_number}/filing-history";
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
    private static final List<String> SUBCATEGORY_LIST = List.of("voluntary", "certificate");
    private static final String CATEGORY = "officers";
    private static final String ACTION_AND_TERMINATION_DATE = "2014-08-29T00:00:00.000Z";
    private static final Instant ACTION_AND_TERMINATION_DATE_AS_INSTANT = Instant.parse(ACTION_AND_TERMINATION_DATE);
    private static final String CONTEXT_ID = "ABCD1234";
    private static final String RESOURCE_CHANGED_URI = "/private/resource-changed";

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.12");

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Supplier<Instant> instantSupplier;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(FILING_HISTORY_COLLECTION);
        mongoTemplate.createCollection(FILING_HISTORY_COLLECTION);
    }

    @Test
    void shouldInsertDocumentAndReturn200OKWhenNoExistingDocumentInDB() throws Exception {
        // given
        final FilingHistoryDocument expectedDocument =
                getExpectedFilingHistoryDocument(null, null, null);
        final InternalFilingHistoryApi request = buildPutRequestBody(NEWEST_REQUEST_DELTA_AT);

        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        stubFor(post(urlEqualTo(RESOURCE_CHANGED_URI))
                .willReturn(aResponse()
                        .withStatus(200)));

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
        WireMock.verify(
                requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldInsertDocumentWithListSubcategoryAndReturn200OKWhenNoExistingDocumentInDB() throws Exception {
        // given
        final FilingHistoryDocument expectedDocument =
                getExpectedFilingHistoryDocument(null, null, null);
        expectedDocument.getData().subcategory(SUBCATEGORY_LIST);

        final InternalFilingHistoryApi request = buildPutRequestBody(NEWEST_REQUEST_DELTA_AT);
        request.getExternalData().subcategory(SUBCATEGORY_LIST);

        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        stubFor(post(urlEqualTo(RESOURCE_CHANGED_URI))
                .willReturn(aResponse()
                        .withStatus(200)));

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
        WireMock.verify(
                requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldUpdateDocumentAndReturn200OKWhenExistingDocumentInDB() throws Exception {
        // given
        final String jsonToInsert = IOUtils.resourceToString("/mongo_docs/filing-history-document.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);

        final FilingHistoryDocument expectedDocument =
                getExpectedFilingHistoryDocument(DOCUMENT_METADATA, 1,
                        List.of(new FilingHistoryAnnotation()
                                .annotation("annotation")
                                .descriptionValues(new FilingHistoryDescriptionValues()
                                        .description("description"))));
        final InternalFilingHistoryApi request = buildPutRequestBody(NEWEST_REQUEST_DELTA_AT);

        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        stubFor(post(urlEqualTo(RESOURCE_CHANGED_URI))
                .willReturn(aResponse()
                        .withStatus(200)));

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

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertNotNull(actualDocument);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(
                requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldGetCompanyFilingHistoryListAndReturn200OK() throws Exception {
        // given
        final FilingHistoryList expectedResponseBody = new FilingHistoryList()
                .itemsPerPage(25)
                .startIndex(0)
                .filingHistoryStatus(FilingHistoryStatusEnum.AVAILABLE)
                .totalCount(1)
                .items(List.of(new ExternalData()
                        .transactionId(TRANSACTION_ID)
                        .barcode(BARCODE)
                        .actionDate("2014-08-29")
                        .category(ExternalData.CategoryEnum.OFFICERS)
                        .type(TM01_TYPE)
                        .description(DESCRIPTION)
                        .subcategory(SUBCATEGORY)
                        .date("2014-09-15")
                        .descriptionValues(new DescriptionValues()
                                .officerName("John Tester")
                                .terminationDate("2014-08-29"))
                        .annotations(List.of(
                                new Annotation()
                                        .annotation("annotation")
                                        .descriptionValues(new DescriptionValues()
                                                .description("description"))))
                        .links(new Links()
                                .self(SELF_LINK)
                                .documentMetadata(
                                        "http://localhost:8080/document/C1_z-KlM567zSgwJz8uN-UZ3_xnGfCljj3k7L69LxwA"))
                        .pages(1)));

        final String jsonToInsert = IOUtils.resourceToString("/mongo_docs/filing-history-document.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<category>", CATEGORY);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);

        // when
        ResultActions result = mockMvc.perform(get(LIST_GET_REQUEST_URI, COMPANY_NUMBER)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final String responseBodyAsString = result.andReturn().getResponse().getContentAsString();
        FilingHistoryList actualResponseBody = objectMapper.readValue(responseBodyAsString, FilingHistoryList.class);

        assertEquals(expectedResponseBody, actualResponseBody);
    }

    @Test
    void shouldGetCompanyFilingHistoryListAndReturn200OKForTopLevelAnnotationWithoutAnnotationsList() throws Exception {
        // given
        final String jsonToInsert = IOUtils.resourceToString("/mongo_docs/top_level_annotation_doc.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);

        // when
        ResultActions result = mockMvc.perform(get(LIST_GET_REQUEST_URI, COMPANY_NUMBER)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final String responseBodyAsString = result.andReturn().getResponse().getContentAsString();
        FilingHistoryList actualResponseBody = objectMapper.readValue(responseBodyAsString, FilingHistoryList.class);

        assertNull(actualResponseBody.getItems().getFirst().getAnnotations());
    }

    @Test
    void shouldGetBaseCompanyFilingHistoryListWhenStatusNotAvailableAndReturn200OK() throws Exception {
        // given
        final FilingHistoryList expectedResponseBody = new FilingHistoryList()
                .itemsPerPage(25)
                .startIndex(0)
                .filingHistoryStatus(FilingHistoryStatusEnum.NOT_AVAILABLE_PROTECTED_CELL_COMPANY)
                .totalCount(0)
                .items(List.of());

        // when
        ResultActions result = mockMvc.perform(get(LIST_GET_REQUEST_URI, "PC000001")
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final String responseBodyAsString = result.andReturn().getResponse().getContentAsString();
        FilingHistoryList actualResponseBody = objectMapper.readValue(responseBodyAsString, FilingHistoryList.class);

        assertEquals(expectedResponseBody, actualResponseBody);
    }

    @Test
    void shouldGetCompanyFilingHistoryListWithPaginationAndFilterAndReturn200OK() throws Exception {
        // given
        final FilingHistoryList expectedResponseBody = new FilingHistoryList()
                .itemsPerPage(100)
                .startIndex(0)
                .filingHistoryStatus(FilingHistoryStatusEnum.AVAILABLE)
                .totalCount(1)
                .items(List.of(new ExternalData()
                        .transactionId(TRANSACTION_ID)
                        .barcode(BARCODE)
                        .actionDate("2014-08-29")
                        .category(ExternalData.CategoryEnum.OFFICERS)
                        .type(TM01_TYPE)
                        .description(DESCRIPTION)
                        .subcategory(SUBCATEGORY)
                        .date("2014-09-15")
                        .descriptionValues(new DescriptionValues()
                                .officerName("John Tester")
                                .terminationDate("2014-08-29"))
                        .annotations(List.of(
                                new Annotation()
                                        .annotation("annotation")
                                        .descriptionValues(new DescriptionValues()
                                                .description("description"))))
                        .links(new Links()
                                .self(SELF_LINK)
                                .documentMetadata(
                                        "http://localhost:8080/document/C1_z-KlM567zSgwJz8uN-UZ3_xnGfCljj3k7L69LxwA"))
                        .pages(1)));

        final String jsonToInsert = IOUtils.resourceToString("/mongo_docs/filing-history-document.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<category>", CATEGORY);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);

        // when
        ResultActions result = mockMvc.perform(
                get(LIST_GET_REQUEST_URI + "?items_per_page=200&category=officers", COMPANY_NUMBER)
                        .header("ERIC-Identity", "123")
                        .header("ERIC-Identity-Type", "key")
                        .header("X-Request-Id", CONTEXT_ID)
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final String responseBodyAsString = result.andReturn().getResponse().getContentAsString();
        FilingHistoryList actualResponseBody = objectMapper.readValue(responseBodyAsString, FilingHistoryList.class);

        assertEquals(expectedResponseBody, actualResponseBody);
    }

    @Test
    void shouldGetSingleDocumentAndReturn200OK() throws Exception {
        // given
        final ExternalData expectedResponseBody = new ExternalData()
                .transactionId(TRANSACTION_ID)
                .barcode(BARCODE)
                .actionDate("2014-08-29")
                .category(ExternalData.CategoryEnum.OFFICERS)
                .type(TM01_TYPE)
                .description(DESCRIPTION)
                .subcategory(SUBCATEGORY)
                .date("2014-09-15")
                .descriptionValues(new DescriptionValues()
                        .officerName("John Tester")
                        .terminationDate("2014-08-29"))
                .annotations(List.of(
                        new Annotation()
                                .annotation("annotation")
                                .descriptionValues(new DescriptionValues()
                                        .description("description"))))
                .links(new Links()
                        .self(SELF_LINK)
                        .documentMetadata("http://localhost:8080/document/C1_z-KlM567zSgwJz8uN-UZ3_xnGfCljj3k7L69LxwA"))
                .pages(1);

        final String jsonToInsert = IOUtils.resourceToString("/mongo_docs/filing-history-document.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<category>", CATEGORY);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);

        // when
        ResultActions result = mockMvc.perform(get(SINGLE_GET_REQUEST_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final String responseBodyAsString = result.andReturn().getResponse().getContentAsString();
        ExternalData actualResponseBody = objectMapper.readValue(responseBodyAsString, ExternalData.class);

        assertEquals(expectedResponseBody, actualResponseBody);
    }

    @Test
    void shouldGetSingleDocumentWithListSubcategoryAndReturn200OK() throws Exception {
        // given
        final ExternalData expectedResponseBody = new ExternalData()
                .transactionId(TRANSACTION_ID)
                .actionDate("2014-08-29")
                .category(CategoryEnum.INSOLVENCY)
                .type("4.38")
                .description("liquidation-voluntary-removal-liquidator")
                .subcategory(SUBCATEGORY_LIST)
                .date("2014-09-15")
                .links(new Links()
                        .self(SELF_LINK)
                        .documentMetadata("http://localhost:8080/document/C1_z-KlM567zSgwJz8uN-UZ3_xnGfCljj3k7L69LxwA"))
                .pages(1)
                .paperFiled(true);

        final String jsonToInsert = IOUtils.resourceToString(
                        "/mongo_docs/filing-history-document-list-subcategory.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);

        // when
        ResultActions result = mockMvc.perform(get(SINGLE_GET_REQUEST_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final String responseBodyAsString = result.andReturn().getResponse().getContentAsString();
        ExternalData actualResponseBody = objectMapper.readValue(responseBodyAsString, ExternalData.class);

        assertEquals(expectedResponseBody, actualResponseBody);
    }

    @Test
    void shouldDeleteDocumentAndReturn200OKWhenExistingDocumentInDB() throws Exception {
        // given
        final String jsonToInsert = IOUtils.resourceToString("/mongo_docs/filing-history-document.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<category>", CATEGORY);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);

        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        stubFor(post(urlEqualTo(RESOURCE_CHANGED_URI))
                .willReturn(aResponse()
                        .withStatus(200)));

        // when
        final ResultActions result = mockMvc.perform(delete(DELETE_REQUEST_URI, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header("X-Request-Id", "ABCD1234")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertNull(actualDocument);

        verify(instantSupplier, times(1)).get();
        WireMock.verify(requestMadeFor(
                new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResourceDelete())));
    }

    @Test
    void shouldReturn400BadRequestWhenInvalidFieldsSentInRequestBody() throws Exception {
        // given
        InternalFilingHistoryApi requestBody = new InternalFilingHistoryApi()
                .externalData(new ExternalData()
                        .type(TM01_TYPE)
                        .date(DATE)
                        .category(ExternalData.CategoryEnum.OFFICERS)
                        .description(DESCRIPTION)
                        .links(new Links()
                                .self(SELF_LINK)))
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .transactionKind(TransactionKindEnum.TOP_LEVEL)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT));

        // when
        ResultActions result = mockMvc.perform(put(PUT_REQUEST_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));

        // then
        result.andExpect(MockMvcResultMatchers.status().isBadRequest());
        assertNull(mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class));
    }

    @Test
    void shouldReturn401UnauthorisedWhenNoIdentity() throws Exception {
        // given

        // when
        ResultActions result = mockMvc.perform(put(PUT_REQUEST_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildPutRequestBody(NEWEST_REQUEST_DELTA_AT))));

        // then
        result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
        assertNull(mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class));
    }

    @Test
    void shouldReturn403ForbiddenWhenNoInternalAppPrivileges() throws Exception {
        // given

        // when
        ResultActions result = mockMvc.perform(put(PUT_REQUEST_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildPutRequestBody(NEWEST_REQUEST_DELTA_AT))));

        // then
        result.andExpect(MockMvcResultMatchers.status().isForbidden());
        assertNull(mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class));
    }

    @Test
    void shouldNotUpdateDocumentAndShouldReturn409ConflictWhenDeltaStale() throws Exception {
        // given
        final String jsonToInsert = IOUtils.resourceToString("/mongo_docs/filing-history-document.json",
                        StandardCharsets.UTF_8)
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
    void shouldReturn404NotFoundWhenNoDocumentInDB() throws Exception {
        // given

        // when
        ResultActions result = mockMvc.perform(get(SINGLE_GET_REQUEST_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void shouldReturn404WhenGetCompanyFilingHistoryAndNoDocumentInDB() throws Exception {
        // given
        final FilingHistoryList expectedResponseBody = new FilingHistoryList()
                .itemsPerPage(25)
                .startIndex(0)
                .filingHistoryStatus(FilingHistoryStatusEnum.AVAILABLE)
                .totalCount(0)
                .items(List.of());

        // when
        ResultActions result = mockMvc.perform(get(LIST_GET_REQUEST_URI, COMPANY_NUMBER)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        final String responseBodyAsString = result.andReturn().getResponse().getContentAsString();
        FilingHistoryList actualResponseBody = objectMapper.readValue(responseBodyAsString, FilingHistoryList.class);

        assertEquals(expectedResponseBody, actualResponseBody);
    }

    @Test
    void shouldReturn503ServiceUnavailableWhenChsKafkaApiReturnsA503ResponseAndNoDocumentShouldBeInDB()
            throws Exception {
        // given
        final InternalFilingHistoryApi request = buildPutRequestBody(NEWEST_REQUEST_DELTA_AT);

        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        stubFor(post(urlEqualTo(RESOURCE_CHANGED_URI))
                .willReturn(aResponse()
                        .withStatus(503)));

        // when
        ResultActions result = mockMvc.perform(put(PUT_REQUEST_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(MockMvcResultMatchers.status().isServiceUnavailable());

        assertNull(mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class));

        verify(instantSupplier, times(2)).get();
        WireMock.verify(
                requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldReturn503ServiceUnavailableWhenChsKafkaApiReturnsA503ResponseOnUpsertAndDocumentShouldBeRolledBackToPreviousState()
            throws Exception {
        // given
        final String jsonToInsert = IOUtils.resourceToString("/mongo_docs/filing-history-document.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);

        final FilingHistoryDocument expectedDocument = mongoTemplate.findById(TRANSACTION_ID,
                FilingHistoryDocument.class);

        final InternalFilingHistoryApi request = buildPutRequestBody(NEWEST_REQUEST_DELTA_AT);

        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        stubFor(post(urlEqualTo(RESOURCE_CHANGED_URI))
                .willReturn(aResponse()
                        .withStatus(503)));

        // when
        ResultActions result = mockMvc.perform(put(PUT_REQUEST_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(MockMvcResultMatchers.status().isServiceUnavailable());

        assertEquals(expectedDocument, mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class));

        verify(instantSupplier, times(2)).get();
        WireMock.verify(
                requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldReturn503ServiceUnavailableWhenChsKafkaApiReturnsA503ResponseOnDeleteAndDocumentShouldBeRolledBackToPreviousState()
            throws Exception {
        // given
        final String jsonToInsert = IOUtils.resourceToString("/mongo_docs/filing-history-document.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<category>", CATEGORY);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);

        final FilingHistoryDocument expectedDocument = mongoTemplate.findById(TRANSACTION_ID,
                FilingHistoryDocument.class);

        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        stubFor(post(urlEqualTo(RESOURCE_CHANGED_URI))
                .willReturn(aResponse()
                        .withStatus(503)));

        // when
        final ResultActions result = mockMvc.perform(delete(DELETE_REQUEST_URI, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header("X-Request-Id", "ABCD1234")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isServiceUnavailable());

        assertEquals(expectedDocument, mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class));

        verify(instantSupplier, times(1)).get();
        WireMock.verify(
                requestMadeFor(
                        new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResourceDelete())));
    }

    @Test
    void shouldReturn404NotFoundWheDocumentCannotBeFoundOnDelete()
            throws Exception {
        // given

        // when
        final ResultActions result = mockMvc.perform(delete(DELETE_REQUEST_URI, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header("X-Request-Id", "ABCD1234")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void shouldUpdateDocumentAndReturn200OKWhenExistingDocumentHasNoDeltaAt() throws Exception {
        // given
        final String jsonToInsert = IOUtils.resourceToString("/mongo_docs/filing-history-document.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER);
        Document doc = Document.parse(jsonToInsert);
        doc.remove("delta_at");
        mongoTemplate.insert(doc, FILING_HISTORY_COLLECTION);

        final FilingHistoryDocument expectedDocument =
                getExpectedFilingHistoryDocument(DOCUMENT_METADATA, 1,
                        List.of(new FilingHistoryAnnotation()
                                .annotation("annotation")
                                .descriptionValues(new FilingHistoryDescriptionValues()
                                        .description("description"))));
        final InternalFilingHistoryApi request = buildPutRequestBody(NEWEST_REQUEST_DELTA_AT);

        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        stubFor(post(urlEqualTo(RESOURCE_CHANGED_URI))
                .willReturn(aResponse()
                        .withStatus(200)));

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

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertNotNull(actualDocument);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(
                requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
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
                .subcategory(SUBCATEGORY)
                .description(DESCRIPTION)
                .descriptionValues(new DescriptionValues()
                        .officerName(OFFICER_NAME)
                        .terminationDate(ACTION_AND_TERMINATION_DATE))
                .pages(1) // should not be mapped, persisted by document store sub delta
                .actionDate(ACTION_AND_TERMINATION_DATE)
                .links(new Links()
                        .self(SELF_LINK));
    }

    private static FilingHistoryDocument getExpectedFilingHistoryDocument(final String documentMetadata,
            Integer pages,
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
                        .descriptionValues(new FilingHistoryDescriptionValues()
                                .terminationDate(ACTION_AND_TERMINATION_DATE_AS_INSTANT)
                                .officerName(OFFICER_NAME))
                        .annotations(annotations)
                        .links(new FilingHistoryLinks()
                                .documentMetadata(documentMetadata)
                                .self(SELF_LINK))
                        .pages(pages)
                        .paperFiled(null))
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

    private static String getExpectedChangedResource() throws IOException {
        return IOUtils.resourceToString("/resource_changed/expected-resource-changed.json", StandardCharsets.UTF_8)
                .replaceAll("<published_at>", UPDATED_AT.toString());
    }

    private static String getExpectedChangedResourceDelete() throws IOException {
        return IOUtils.resourceToString("/resource_changed/expected-delete-resource-changed.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<published_at>", UPDATED_AT.toString());
    }
}
