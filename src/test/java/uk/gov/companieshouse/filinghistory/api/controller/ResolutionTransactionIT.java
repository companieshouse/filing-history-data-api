package uk.gov.companieshouse.filinghistory.api.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.requestMadeFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.core.JsonProcessingException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryList;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryList.FilingHistoryStatusEnum;
import uk.gov.companieshouse.api.filinghistory.Links;
import uk.gov.companieshouse.api.filinghistory.Resolution;
import uk.gov.companieshouse.api.filinghistory.Resolution.CategoryEnum;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
@WireMockTest(httpPort = 8889)
public class ResolutionTransactionIT {

    private static final String PUT_REQUEST_URI = "/filing-history-data-api/company/{company_number}/filing-history/{transaction_id}/internal";
    private static final String GET_SINGLE_TRANSACTION_URI = "/filing-history-data-api/company/{company_number}/filing-history/{transaction_id}";
    private static final String GET_FILING_HISTORY_URI = "/filing-history-data-api/company/{company_number}/filing-history";
    private static final String FILING_HISTORY_COLLECTION = "company_filing_history";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String ENTITY_ID = "1234567890";
    private static final String EXISTING_DELTA_AT = "20140815230459600643";
    private static final String CONTEXT_ID = "ABCD1234";
    private static final String SELF_LINK = "/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID);
    private static final String BARCODE = "AOPYXMJN";
    private static final String NEWEST_REQUEST_DELTA_AT = "20140916230459600643";
    private static final String STALE_REQUEST_DELTA_AT = "20130615185208001000";
    private static final String EXISTING_DELTA_AT_TWO = "20140816230459600643";
    private static final Instant UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);
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
    void shouldReturnSingleGetResponseForResolutions() throws Exception {
        // given
        final String existingDocumentJson = IOUtils.resourceToString(
                        "/mongo_docs/resolutions/existing_resolution_doc.json", StandardCharsets.UTF_8)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<delta_at>", EXISTING_DELTA_AT);
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        ExternalData expectedResponse = new ExternalData()
                .transactionId(TRANSACTION_ID)
                .type("RESOLUTIONS")
                .date("2015-06-10")
                .category(ExternalData.CategoryEnum.RESOLUTION)
                .description("resolution")
                .descriptionValues(new DescriptionValues()
                        .description("Resolutions"))
                .links(new Links()
                        .self("/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID))
                        .documentMetadata("http://localhost:8080/document/oGimUKFCtvKUJRbkuupRh-0arENh56Stcn-SZlUSwqI"))
                .paperFiled(true)
                .pages(1)
                .resolutions(List.of(
                        new Resolution()
                                .category(CategoryEnum.LIQUIDATION)
                                .subcategory(List.of("voluntary", "resolution"))
                                .description("liquidation-voluntary-special-resolution-to-wind-up-case-start-date")
                                .descriptionValues(new DescriptionValues()
                                        .caseStartDate("2015-05-12"))
                                .type("LRESSP")
                                .originalDescription("original description")
                                .deltaAt(EXISTING_DELTA_AT),
                        new Resolution()
                                .category(CategoryEnum.LIQUIDATION)
                                .subcategory(List.of("voluntary", "resolution"))
                                .description("liquidation-voluntary-special-resolution-to-wind-up-case-start-date")
                                .descriptionValues(new DescriptionValues()
                                        .caseStartDate("2015-05-12"))
                                .type("LRESSP")
                                .originalDescription("original description")
                                .deltaAt(EXISTING_DELTA_AT)
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
    void shouldReturnListGetResponseForResolutions() throws Exception {
        // given
        final String existingDocumentJson = IOUtils.resourceToString(
                        "/mongo_docs/resolutions/existing_resolution_doc.json", StandardCharsets.UTF_8)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<parent_entity_id>", ENTITY_ID)
                .replaceAll("<delta_at>", EXISTING_DELTA_AT);
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        FilingHistoryList expectedResponse = new FilingHistoryList()
                .itemsPerPage(25)
                .filingHistoryStatus(FilingHistoryStatusEnum.AVAILABLE)
                .totalCount(1)
                .startIndex(0)
                .items(List.of(new ExternalData()
                        .transactionId(TRANSACTION_ID)
                        .description("resolution")
                        .type("RESOLUTIONS")
                        .category(ExternalData.CategoryEnum.RESOLUTION)
                        .links(new Links()
                                .self("/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID))
                                .documentMetadata("http://localhost:8080/document/oGimUKFCtvKUJRbkuupRh-0arENh56Stcn-SZlUSwqI"))
                        .paperFiled(true)
                        .date("2015-06-10")
                        .descriptionValues(new DescriptionValues()
                                .description("Resolutions"))
                        .pages(1)
                        .resolutions(List.of(
                                new Resolution()
                                        .category(CategoryEnum.LIQUIDATION)
                                        .subcategory(List.of("voluntary", "resolution"))
                                        .description("liquidation-voluntary-special-resolution-to-wind-up-case-start-date")
                                        .descriptionValues(new DescriptionValues()
                                                .caseStartDate("2015-05-12"))
                                        .type("LRESSP")
                                        .originalDescription("original description"),
                                new Resolution()
                                        .category(CategoryEnum.LIQUIDATION)
                                        .subcategory(List.of("voluntary", "resolution"))
                                        .description("liquidation-voluntary-special-resolution-to-wind-up-case-start-date")
                                        .descriptionValues(new DescriptionValues()
                                                .caseStartDate("2015-05-12"))
                                        .type("LRESSP")
                                        .originalDescription("original description"))))
                );

        // when
        ResultActions result = mockMvc.perform(get(GET_FILING_HISTORY_URI, COMPANY_NUMBER)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("X-Request-Id", CONTEXT_ID));

        // then
        FilingHistoryList actualResponse = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), FilingHistoryList.class);

        assertEquals(expectedResponse, actualResponse);
    }
    void shouldMapFirstResolutionToTopLevelFields() throws Exception {
        // given
        String expectedDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/resolutions/expected_resolution_doc_with_one_resolution.json", StandardCharsets.UTF_8);
        expectedDocumentJson = expectedDocumentJson
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<first_resolution_entity_id>", ENTITY_ID)
                .replaceAll("<first_resolution_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString());
        final FilingHistoryDocument expectedDocument =
                objectMapper.readValue(expectedDocumentJson, FilingHistoryDocument.class);


        String requestBody = IOUtils.resourceToString(
                "/put_requests/resolutions/put_request_body_resolution.json", StandardCharsets.UTF_8);
        requestBody = requestBody
                .replaceAll("<delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<entity_id>", ENTITY_ID);

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
        assertNotNull(actualDocument);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));

    }

    @Test
    void shouldAddResolutionToExistingResolutionListAndReturn200OK() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/resolutions/existing_resolution_document_with_one_resolution.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<first_resolution_entity_id>", ENTITY_ID)
                .replaceAll("<existing_resolution_entity_id>", "3333333333")
                .replaceAll("<first_resolution_delta_at>", EXISTING_DELTA_AT);
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        String expectedDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/resolutions/expected_resolution_doc_with_two_resolutions.json", StandardCharsets.UTF_8);
        expectedDocumentJson = expectedDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<first_resolution_entity_id>", "3333333333")
                .replaceAll("<first_resolution_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<second_resolution_entity_id>", ENTITY_ID)
                .replaceAll("<second_resolution_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<updated_at>", UPDATED_AT.toString());
        final FilingHistoryDocument expectedDocument =
                objectMapper.readValue(expectedDocumentJson, FilingHistoryDocument.class);

        String requestBody = IOUtils.resourceToString(
                "/put_requests/resolutions/put_request_body_second_resolution.json", StandardCharsets.UTF_8);
        requestBody = requestBody
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<second_resolution_entity_id>", ENTITY_ID)
                .replaceAll("<second_resolution_delta_at>", NEWEST_REQUEST_DELTA_AT);

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
        assertNotNull(actualDocument);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }


    @Test
    void shouldUpdateExistingResolutionOnDocumentAndReturn200OK() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/resolutions/existing_resolution_document_with_one_resolution.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<existing_resolution_entity_id>", ENTITY_ID)
                .replaceAll("<first_resolution_delta_at>", EXISTING_DELTA_AT);
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        String expectedDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/resolutions/expected_resolution_doc_with_one_resolution.json", StandardCharsets.UTF_8);
        expectedDocumentJson = expectedDocumentJson
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("liquidation", "insolvency")
                .replaceAll("<first_resolution_entity_id>", ENTITY_ID)
                .replaceAll("<first_resolution_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<updated_at>", UPDATED_AT.toString());
        final FilingHistoryDocument expectedDocument =
                objectMapper.readValue(expectedDocumentJson, FilingHistoryDocument.class);

        String requestBody = IOUtils.resourceToString(
                "/put_requests/resolutions/put_request_body_resolution.json", StandardCharsets.UTF_8);
        requestBody = requestBody
                .replaceAll("<delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("liquidation", "insolvency")
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<entity_id>", ENTITY_ID);


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
        assertNotNull(actualDocument);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldUpdateSecondResolutionDocumentAndNotChangeFirstAndReturn200OK() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/resolutions/existing_resolution_doc_with_two_resolutions.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<first_resolution_entity_id>", "3333333333")
                .replaceAll("<first_resolution_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<second_resolution_entity_id>", ENTITY_ID)
                .replaceAll("<second_resolution_delta_at>", EXISTING_DELTA_AT_TWO)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<updated_at>", UPDATED_AT.toString());
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        String expectedDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/resolutions/expected_resolution_doc_with_two_resolutions.json", StandardCharsets.UTF_8);
        expectedDocumentJson = expectedDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<first_resolution_entity_id>", "3333333333")
                .replaceAll("<first_resolution_delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<second_resolution_entity_id>", ENTITY_ID)
                .replaceAll("legacy", "current different description")
                .replaceAll("<second_resolution_delta_at>", NEWEST_REQUEST_DELTA_AT)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<updated_at>", UPDATED_AT.toString());
        final FilingHistoryDocument expectedDocument =
                objectMapper.readValue(expectedDocumentJson, FilingHistoryDocument.class);

        String requestBody = IOUtils.resourceToString(
                "/put_requests/resolutions/put_request_body_second_resolution.json", StandardCharsets.UTF_8);
        requestBody = requestBody
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("legacy", "current different description")
                .replaceAll("<second_resolution_entity_id>", ENTITY_ID)
                .replaceAll("<second_resolution_delta_at>", NEWEST_REQUEST_DELTA_AT);

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
        assertNotNull(actualDocument);
        assertEquals(expectedDocument, actualDocument);

        verify(instantSupplier, times(2)).get();
        WireMock.verify(requestMadeFor(new ResourceChangedRequestMatcher(RESOURCE_CHANGED_URI, getExpectedChangedResource())));
    }

    @Test
    void shouldThrowConflictExceptionWhenUpdatingChildAnnotationWithStaleDeltaAtAndReturn409Conflict() throws Exception {
        // given
        String existingDocumentJson = IOUtils.resourceToString(
                "/mongo_docs/resolutions/existing_resolution_document_with_one_resolution.json", StandardCharsets.UTF_8);
        existingDocumentJson = existingDocumentJson
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<existing_resolution_entity_id>", ENTITY_ID)
                .replaceAll("<first_resolution_delta_at>", EXISTING_DELTA_AT);
        final FilingHistoryDocument existingDocument =
                objectMapper.readValue(existingDocumentJson, FilingHistoryDocument.class);
        mongoTemplate.insert(existingDocument, FILING_HISTORY_COLLECTION);

        String requestBody = IOUtils.resourceToString(
                "/put_requests/resolutions/put_request_body_resolution.json", StandardCharsets.UTF_8);
        requestBody = requestBody
                .replaceAll("<delta_at>", STALE_REQUEST_DELTA_AT)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("liquidation", "insolvency")
                .replaceAll("<transaction_id>", TRANSACTION_ID)
                .replaceAll("<barcode>", BARCODE)
                .replaceAll("<entity_id>", ENTITY_ID);


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
        assertNotNull(actualDocument);

        // Assert existing doc is unchanged
        assertEquals(existingDocument, actualDocument);

        verifyNoMoreInteractions(instantSupplier);
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
}
