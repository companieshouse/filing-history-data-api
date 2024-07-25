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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Supplier;
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
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.filinghistory.AssociatedFiling;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryList;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryList.FilingHistoryStatusEnum;
import uk.gov.companieshouse.api.filinghistory.Links;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAssociatedFiling;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
@WireMockTest(httpPort = 8889)
class AssociatedFilingTransactionIT {

    private static final String PUT_REQUEST_URI = "/company/{company_number}/filing-history/{transaction_id}/internal";
    private static final String GET_SINGLE_TRANSACTION_URI = "/company/{company_number}/filing-history/{transaction_id}";
    private static final String GET_FILING_HISTORY_URI = "/company/{company_number}/filing-history";
    private static final String DELETE_REQUEST_URI = "/filing-history/{entity_id}/internal";
    private static final String FILING_HISTORY_COLLECTION = "company_filing_history";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String SELF_LINK = "/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID);
    private static final String ENTITY_ID = "1234567890";
    private static final String CHILD_ENTITY_ID = "2234567890";
    private static final String EXISTING_CHILD_ENTITY_ID = "3234567890";
    private static final String BARCODE = "X4BI89B6";
    private static final String NEWEST_REQUEST_DELTA_AT = "20140916230459600643";
    private static final String STALE_REQUEST_DELTA_AT = "20130615185208001000";
    private static final String EXISTING_DELTA_AT = "20140815230459600643";
    private static final Instant UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    private static final String CONTEXT_ID = "ABCD1234";
    private static final String RESOURCE_CHANGED_URI = "/private/resource-changed";
    private static final String EXISTING_DATE = "2012-06-08T11:57:11Z";

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
    void shouldAddAssociatedFilingToExistingDocumentAndReturn200OK() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_parent_doc_with_zero_associated_filings.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        String expectedDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/expected_parent_doc_with_one_associated_filing.json", StandardCharsets.UTF_8);
        expectedDocumentJson = expectedDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<child_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<context_id>", CONTEXT_ID)
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument expectedDocument =
                objectMapper.readValue(expectedDocumentJson, FilingHistoryDocument.class);

        String requestBody = IOUtils.resourceToString(
                "/put_requests/associated_filings/put_request_body_associated_filing.json", StandardCharsets.UTF_8);
        requestBody = requestBody
                .replaceAll("<delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<context_id>", CONTEXT_ID);

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
                .content(requestBody));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.header().string(LOCATION, SELF_LINK));

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldAddAssociatedFilingToExistingAssociatedFilingListAndReturn200OK() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_parent_doc_with_associated_filing.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<existing_child_entity_id>", "3333333333")
                .replaceAll("<existing_child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        String expectedDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/expected_parent_doc_with_two_associated_filings.json", StandardCharsets.UTF_8);
        expectedDocumentJson = expectedDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<existing_child_entity_id>", "3333333333")
                .replaceAll("<existing_child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<child_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<context_id>", CONTEXT_ID)
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument expectedDocument =
                objectMapper.readValue(expectedDocumentJson, FilingHistoryDocument.class);

        String requestBody = IOUtils.resourceToString(
                "/put_requests/associated_filings/put_request_body_associated_filing.json", StandardCharsets.UTF_8);
        requestBody = requestBody
                .replaceAll("<delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<context_id>", CONTEXT_ID);

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
                .content(requestBody));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.header().string(LOCATION, SELF_LINK));

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldInsertNoParentAssociatedFilingDocumentAndReturn200OK() throws Exception {
        // given
        String expectedDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/expected_associated_filing_doc_with_no_parent.json", StandardCharsets.UTF_8);
        expectedDocumentJson = expectedDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<child_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<context_id>", CONTEXT_ID)
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument expectedDocument =
                objectMapper.readValue(expectedDocumentJson, FilingHistoryDocument.class);

        String requestBody = IOUtils.resourceToString(
                "/put_requests/associated_filings/put_request_body_associated_filing.json", StandardCharsets.UTF_8);
        requestBody = requestBody
                .replaceAll("<delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<context_id>", CONTEXT_ID);

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
                .content(requestBody));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.header().string(LOCATION, SELF_LINK));

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldUpdateNoParentDocumentWithParentFieldsAndNotChangeAssociatedFilingAndReturn200OK() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_associated_filing_doc_with_no_parent.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<child_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        String expectedDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/expected_parent_doc_with_one_associated_filing.json", StandardCharsets.UTF_8);
        expectedDocumentJson = expectedDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<parent_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<context_id>", CONTEXT_ID)
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument expectedDocument =
                objectMapper.readValue(expectedDocumentJson, FilingHistoryDocument.class);

        String requestBody = IOUtils.resourceToString(
                "/put_requests/tm01s/put_request_TM01.json", StandardCharsets.UTF_8);
        requestBody = requestBody
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<entity_id>", ENTITY_ID)
                .replaceAll("<parent_entity_id>", "")
                .replaceAll("<context_id>", CONTEXT_ID)
                .replaceAll("<barcode>", BARCODE);

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
                .content(requestBody));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.header().string(LOCATION, SELF_LINK));

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldUpdateExistingAssociatedFilingAndReturn200OK() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_parent_doc_with_associated_filing.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<existing_child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<existing_child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        String expectedDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/expected_parent_doc_with_one_associated_filing.json", StandardCharsets.UTF_8);
        expectedDocumentJson = expectedDocumentJson
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<child_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<context_id>", CONTEXT_ID)
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument expectedDocument =
                objectMapper.readValue(expectedDocumentJson, FilingHistoryDocument.class);

        String requestBody = IOUtils.resourceToString(
                "/put_requests/associated_filings/put_request_body_associated_filing.json", StandardCharsets.UTF_8);
        requestBody = requestBody
                .replaceAll("<delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<context_id>", CONTEXT_ID);

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
                .content(requestBody));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.header().string(LOCATION, SELF_LINK));

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldUpdateParentDocumentAndNotChangeAssociatedFilingAndReturn200OK() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_parent_doc_with_associated_filing.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<existing_child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<existing_child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        String expectedDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/expected_parent_doc_with_one_associated_filing.json", StandardCharsets.UTF_8);
        expectedDocumentJson = expectedDocumentJson
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<parent_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<context_id>", CONTEXT_ID)
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument expectedDocument =
                objectMapper.readValue(expectedDocumentJson, FilingHistoryDocument.class);

        String requestBody = IOUtils.resourceToString(
                "/put_requests/tm01s/put_request_TM01.json", StandardCharsets.UTF_8);
        requestBody = requestBody
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<entity_id>", ENTITY_ID)
                .replaceAll("<context_id>", CONTEXT_ID)
                .replaceAll("<parent_entity_id>", "")
                .replaceAll("<barcode>", BARCODE);

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
                .content(requestBody));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.header().string(LOCATION, SELF_LINK));

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldThrowConflictExceptionWhenUpdatingChildAssociatedFilingWithStaleDeltaAtAndReturn409Conflict() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_parent_doc_with_associated_filing.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<existing_child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<existing_child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        String requestBody = IOUtils.resourceToString(
                "/put_requests/associated_filings/put_request_body_associated_filing.json", StandardCharsets.UTF_8);
        requestBody = requestBody
                .replaceAll("<delta_at>", STALE_REQUEST_DELTA_AT)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<context_id>", CONTEXT_ID);

        // when
        ResultActions result = mockMvc.perform(put(PUT_REQUEST_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(MockMvcResultMatchers.status().isConflict());

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);

        // Assert existing doc is unchanged
        assertEquals(existingDocument, actualDocument);

        verify(instantSupplier).get();
    }

    @Test
    void shouldSuccessfullyReturnSingleGetResponse() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_associated_filing_doc_with_action_date_and_document_metadata.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<barcode>", "")
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<existing_child_entity_id>", "3333333333")
                .replaceAll("<existing_child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        ExternalData expectedResponse = new ExternalData()
                .transactionId(TRANSACTION_ID)
                .type("TM01")
                .date("2014-09-15")
                .category(CategoryEnum.OFFICERS)
                .description("termination-director-company-with-name-termination-date")
                .subcategory("termination")
                .descriptionValues(new DescriptionValues()
                        .officerName("John Test Tester")
                        .terminationDate("2014-09-15"))
                .actionDate("2014-09-15")
                .links(new Links()
                        .self("/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID))
                        .documentMetadata("http://localhost:8080/document/document_metadata_id"))
                .associatedFilings(List.of(
                        new AssociatedFiling()
                                .actionDate("2015-10-05")
                                .category("annual-return")
                                .date("2005-05-10")
                                .description("legacy")
                                .descriptionValues(new DescriptionValues()
                                        .description("Secretary's particulars changed;director's particulars changed"))
                                .type("363(288)")
                ));

        // when
        ResultActions result = mockMvc.perform(get(GET_SINGLE_TRANSACTION_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("X-Request-Id", CONTEXT_ID));

        // then
        ExternalData actualResponse = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), ExternalData.class);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void shouldReturnListGetResponseWithNoOriginalDescriptionFieldOnChildObject() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_parent_doc_with_associated_filing.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<barcode>", "")
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<existing_child_entity_id>", "3333333333")
                .replaceAll("<existing_child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        FilingHistoryList expectedResponse = new FilingHistoryList()
                .items(List.of(new ExternalData()
                        .transactionId(TRANSACTION_ID)
                        .type("TM01")
                        .date("2014-09-15")
                        .category(CategoryEnum.OFFICERS)
                        .description("termination-director-company-with-name-termination-date")
                        .subcategory("termination")
                        .descriptionValues(new DescriptionValues()
                                .officerName("John Test Tester")
                                .terminationDate("2014-09-15"))
                        .actionDate("2014-09-15")
                        .links(new Links()
                                .self("/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID)))
                        .associatedFilings(List.of(
                                new AssociatedFiling()
                                        .category("annual-return")
                                        .date("2005-05-10")
                                        .description("legacy")
                                        .descriptionValues(new DescriptionValues()
                                                .description("Secretary's particulars changed;director's particulars changed"))
                                        .type("363(288)")
                        ))
                ))
                .itemsPerPage(25)
                .totalCount(1)
                .filingHistoryStatus(FilingHistoryStatusEnum.AVAILABLE)
                .startIndex(0);

        // when
        ResultActions result = mockMvc.perform(get(GET_FILING_HISTORY_URI, COMPANY_NUMBER)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("X-Request-Id", CONTEXT_ID));

        // then
        FilingHistoryList actualResponse = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), FilingHistoryList.class);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void shouldUpdateExistingAssociatedFilingWhenChildHasNoDeltaAt() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_parent_doc_with_associated_filing.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<existing_child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<existing_child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);

        existingDocument.getData().getAssociatedFilings().getFirst().deltaAt(null);

        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        String expectedDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/expected_parent_doc_with_one_associated_filing.json", StandardCharsets.UTF_8);
        expectedDocumentJson = expectedDocumentJson
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<child_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<context_id>", CONTEXT_ID)
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument expectedDocument =
                objectMapper.readValue(expectedDocumentJson, FilingHistoryDocument.class);

        String requestBody = IOUtils.resourceToString(
                "/put_requests/associated_filings/put_request_body_associated_filing.json", StandardCharsets.UTF_8);
        requestBody = requestBody
                .replaceAll("<delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<context_id>", CONTEXT_ID);

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
                .content(requestBody));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.header().string(LOCATION, SELF_LINK));

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldAddChildToListWhenExistingListContainsChildWithNoEntityId() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_parent_doc_with_associated_filing.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<existing_child_entity_id>", EXISTING_CHILD_ENTITY_ID)
                .replaceAll("<existing_child_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);

        existingDocument.getData().getAssociatedFilings().getFirst().entityId(null);

        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        String expectedDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/expected_parent_doc_with_two_associated_filings.json", StandardCharsets.UTF_8);
        expectedDocumentJson = expectedDocumentJson
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<existing_child_entity_id>", EXISTING_CHILD_ENTITY_ID)
                .replaceAll("<child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<existing_child_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<child_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<context_id>", CONTEXT_ID)
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument expectedDocument =
                objectMapper.readValue(expectedDocumentJson, FilingHistoryDocument.class);

        expectedDocument.getData().getAssociatedFilings().getFirst().entityId(null);

        String requestBody = IOUtils.resourceToString(
                "/put_requests/associated_filings/put_request_body_associated_filing.json", StandardCharsets.UTF_8);
        requestBody = requestBody
                .replaceAll("<delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<context_id>", CONTEXT_ID);

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
                .content(requestBody));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.header().string(LOCATION, SELF_LINK));

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldSuccessfullyHandleSingleGetWhenDealingWithLegacyData() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_parent_doc_with_associated_filing.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<barcode>", "")
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<existing_child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<existing_child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);

        FilingHistoryAssociatedFiling firstAssociatedFiling = existingDocument.getData().getAssociatedFilings().getFirst();
        firstAssociatedFiling.deltaAt(null);
        firstAssociatedFiling.entityId(null);

        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        ExternalData expectedResponse = new ExternalData()
                .transactionId(TRANSACTION_ID)
                .type("TM01")
                .date("2014-09-15")
                .category(CategoryEnum.OFFICERS)
                .description("termination-director-company-with-name-termination-date")
                .subcategory("termination")
                .descriptionValues(new DescriptionValues()
                        .officerName("John Test Tester")
                        .terminationDate("2014-09-15"))
                .actionDate("2014-09-15")
                .links(new Links()
                        .self("/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID)))
                .associatedFilings(List.of(
                        new AssociatedFiling()
                                .category("annual-return")
                                .date("2005-05-10")
                                .description("legacy")
                                .descriptionValues(new DescriptionValues()
                                        .description("Secretary's particulars changed;director's particulars changed"))
                                .type("363(288)")
                ));

        // when
        ResultActions result = mockMvc.perform(get(GET_SINGLE_TRANSACTION_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("X-Request-Id", CONTEXT_ID));

        // then
        ExternalData actualResponse = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), ExternalData.class);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void shouldSuccessfullyHandleListGetWhenDealingWithLegacyData() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_parent_doc_with_associated_filing.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<barcode>", "")
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<existing_child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<existing_child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);

        FilingHistoryAssociatedFiling firstAssociatedFiling = existingDocument.getData().getAssociatedFilings().getFirst();
        firstAssociatedFiling.deltaAt(null);
        firstAssociatedFiling.entityId(null);

        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        FilingHistoryList expectedObject = new FilingHistoryList()
                .items(List.of(new ExternalData()
                        .transactionId(TRANSACTION_ID)
                        .type("TM01")
                        .date("2014-09-15")
                        .category(CategoryEnum.OFFICERS)
                        .description("termination-director-company-with-name-termination-date")
                        .subcategory("termination")
                        .descriptionValues(new DescriptionValues()
                                .officerName("John Test Tester")
                                .terminationDate("2014-09-15"))
                        .actionDate("2014-09-15")
                        .links(new Links()
                                .self("/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID)))
                        .associatedFilings(List.of(
                                new AssociatedFiling()
                                        .category("annual-return")
                                        .date("2005-05-10")
                                        .description("legacy")
                                        .descriptionValues(new DescriptionValues()
                                                .description("Secretary's particulars changed;director's particulars changed"))
                                        .type("363(288)")
                        ))))
                .itemsPerPage(25)
                .totalCount(1)
                .filingHistoryStatus(FilingHistoryList.FilingHistoryStatusEnum.AVAILABLE)
                .startIndex(0);

        // when
        ResultActions result = mockMvc.perform(get(GET_FILING_HISTORY_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("X-Request-Id", CONTEXT_ID));

        // then
        final String actualResponse = result.andReturn().getResponse().getContentAsString();
        final String expectedResponse = objectMapper.writeValueAsString(expectedObject);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void shouldDeleteSingularAssociatedFilingFromParentWithTwoAssociatedFilings() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_parent_doc_with_two_associated_filings.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<existing_child_entity_id>", EXISTING_CHILD_ENTITY_ID)
                .replaceAll("<child_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<existing_child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<updated_at>", EXISTING_DATE)
                .replaceAll("<created_at>", EXISTING_DATE);
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        String expectedDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/expected_parent_doc_with_one_associated_filing.json", StandardCharsets.UTF_8);
        expectedDocumentJson = expectedDocumentJson
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<child_entity_id>", EXISTING_CHILD_ENTITY_ID)
                .replaceAll("<child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<created_at>", EXISTING_DATE)
                .replaceAll("<context_id>", CONTEXT_ID);

        final FilingHistoryDocument expectedDocument =
                objectMapper.readValue(expectedDocumentJson, FilingHistoryDocument.class);

        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        stubFor(post(urlEqualTo(RESOURCE_CHANGED_URI))
                .willReturn(aResponse()
                        .withStatus(200)));

        // when
        ResultActions result = mockMvc.perform(delete(DELETE_REQUEST_URI, CHILD_ENTITY_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(
                requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldDeleteLastAssociatedFilingAndArrayFromParent() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_parent_doc_with_associated_filing.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<existing_child_entity_id>", EXISTING_CHILD_ENTITY_ID)
                .replaceAll("<child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", EXISTING_DATE)
                .replaceAll("<created_at>", EXISTING_DATE);
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        String expectedDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/expected_parent_doc_with_zero_associated_filings.json", StandardCharsets.UTF_8);
        expectedDocumentJson = expectedDocumentJson
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<context_id>", CONTEXT_ID)
                .replaceAll("<created_at>", EXISTING_DATE);
        final FilingHistoryDocument expectedDocument =
                objectMapper.readValue(expectedDocumentJson, FilingHistoryDocument.class);

        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        stubFor(post(urlEqualTo(RESOURCE_CHANGED_URI))
                .willReturn(aResponse()
                        .withStatus(200)));

        // when
        ResultActions result = mockMvc.perform(delete(DELETE_REQUEST_URI, EXISTING_CHILD_ENTITY_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(
                requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldDeleteWholeDocumentForNoParentAssociatedFiling() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_associated_filing_doc_with_no_parent.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<parent_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        stubFor(post(urlEqualTo(RESOURCE_CHANGED_URI))
                .willReturn(aResponse()
                        .withStatus(200)));

        // when
        ResultActions result = mockMvc.perform(delete(DELETE_REQUEST_URI, CHILD_ENTITY_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertNull(actualDocument);

        verify(instantSupplier).get();
        WireMock.verify(
                requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedResourceDeleted(
                ))));
    }

    @Test
    void shouldRollbackParentWithAssociatedFilingAfterRemoveAssociatedFilingButChsKafkaApiUnavailable() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_parent_doc_with_associated_filing.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<existing_child_entity_id>", EXISTING_CHILD_ENTITY_ID)
                .replaceAll("<child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<parent_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<updated_at>", EXISTING_DATE)
                .replaceAll("<created_at>", EXISTING_DATE);
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        FilingHistoryDocument expectedDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertNotNull(expectedDocument);
        expectedDocument.version(expectedDocument.getVersion() + 2);

        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        stubFor(post(urlEqualTo(RESOURCE_CHANGED_URI))
                .willReturn(aResponse()
                        .withStatus(502)));

        // when
        ResultActions result = mockMvc.perform(delete(DELETE_REQUEST_URI, EXISTING_CHILD_ENTITY_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isBadGateway());

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(
                requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldRollbackNoParentAssociatedFilingAfterDeleteButChsKafkaApiUnavailable() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/associated_filings/existing_associated_filing_doc_with_no_parent.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<child_entity_id>", CHILD_ENTITY_ID)
                .replaceAll("<child_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<parent_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString())
                .replaceAll("<created_at>", UPDATED_AT.toString());
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        FilingHistoryDocument expectedDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);

        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        stubFor(post(urlEqualTo(RESOURCE_CHANGED_URI))
                .willReturn(aResponse()
                        .withStatus(502)));

        // when
        ResultActions result = mockMvc.perform(delete(DELETE_REQUEST_URI, CHILD_ENTITY_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header("X-Request-Id", CONTEXT_ID)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isBadGateway());

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier).get();
        WireMock.verify(
                requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedResourceDeleted(
                ))));
    }

    private String getExpectedChangedResource() throws JsonProcessingException {
        return objectMapper.writeValueAsString(new ChangedResource()
                .resourceUri("/company/12345678/filing-history/transactionId")
                .resourceKind("filing-history")
                .contextId(CONTEXT_ID)
                .deletedData(null)
                .event(new ChangedResourceEvent()
                        .fieldsChanged(null)
                        .publishedAt(UPDATED_AT.toString())
                        .type("changed")));
    }

    private static String getExpectedResourceDeleted() throws IOException {
        return IOUtils.resourceToString("/resource_changed/expected-associated-filing-resource-deleted.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<published_at>", UPDATED_AT.toString())
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<updated_at>", EXISTING_DATE)
                .replaceAll("<created_at>", EXISTING_DATE)
                .replaceAll("<context_id>", CONTEXT_ID);
    }
}
