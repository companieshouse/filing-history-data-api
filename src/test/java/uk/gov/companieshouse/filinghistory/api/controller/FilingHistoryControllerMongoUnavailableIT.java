package uk.gov.companieshouse.filinghistory.api.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.time.Instant;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeAll;
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
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.repository.Repository;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
@WireMockTest(httpPort = 8888)
class FilingHistoryControllerMongoUnavailableIT {

    private static final String PUT_REQUEST_URI = "/filing-history-data-api/company/{company_number}/filing-history/{transaction_id}/internal";
    private static final String FILING_HISTORY_COLLECTION = "company_filing_history";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String SELF_LINK = "/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID);
    private static final String ENTITY_ID = "1234567890";
    private static final String DOCUMENT_ID = "000X4BI89B65846";
    private static final String BARCODE = "X4BI89B6";
    private static final String NEWEST_REQUEST_DELTA_AT = "20140916230459600643";
    private static final String UPDATED_BY = "5419d856b6a59f32b7684d0e";
    private static final String TM01_TYPE = "TM01";
    private static final String DATE = "2014-09-15T23:21:18.000Z";
    private static final String ORIGINAL_DESCRIPTION = "Appointment Terminated, Director john tester";
    private static final String OFFICER_NAME = "John Tester";
    private static final String RESIGNATION_DATE = "29/08/2014";
    private static final String DESCRIPTION = "termination-director-company-with-name-termination-date";
    private static final String ACTION_AND_TERMINATION_DATE = "2014-08-29T00:00:00.000Z";
    private static final String SUBCATEGORY = "termination";
    private static final String CONTEXT_ID = "ABCD1234";
    private static final String RESOURCE_CHANGED_URI = "/private/resource-changed";

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.12");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Supplier<Instant> instantSupplier;
    @MockBean
    private Repository repository;

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
    void shouldReturn503WhenRepositoryThrowsServiceUnavailableDuringSave() throws Exception {
        // given
        final InternalFilingHistoryApi request = buildPutRequestBody();

        doThrow(ServiceUnavailableException.class)
                .when(repository).save(any());

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

        verify(instantSupplier).get();
        WireMock.verify(exactly(0), postRequestedFor(urlEqualTo(RESOURCE_CHANGED_URI)));
    }

    @Test
    void shouldReturn503WhenRepositoryThrowsServiceUnavailableDuringFind() throws Exception {
        // given
        final InternalFilingHistoryApi request = buildPutRequestBody();

        when(repository.findByIdAndCompanyNumber(any(), any())).thenThrow(ServiceUnavailableException.class);

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

        verify(instantSupplier, times(0)).get();
        WireMock.verify(exactly(0), postRequestedFor(urlEqualTo(RESOURCE_CHANGED_URI)));
    }

    private static InternalFilingHistoryApi buildPutRequestBody() {
        return new InternalFilingHistoryApi()
                .internalData(buildInternalData())
                .externalData(buildExternalData());
    }

    private static InternalData buildInternalData() {
        return new InternalData()
                .entityId(ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .documentId(DOCUMENT_ID)
                .deltaAt(NEWEST_REQUEST_DELTA_AT)
                .originalDescription(ORIGINAL_DESCRIPTION)
                .originalValues(new InternalDataOriginalValues()
                        .officerName(OFFICER_NAME)
                        .resignationDate(RESIGNATION_DATE))
                .parentEntityId("parent_entity_id")
                .updatedBy(UPDATED_BY)
                .transactionKind(InternalData.TransactionKindEnum.TOP_LEVEL);
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
                .descriptionValues(new FilingHistoryItemDataDescriptionValues()
                        .officerName(OFFICER_NAME)
                        .terminationDate(ACTION_AND_TERMINATION_DATE))
                .pages(1) // should not be mapped, persisted by document store sub delta
                .actionDate(ACTION_AND_TERMINATION_DATE)
                .links(new FilingHistoryItemDataLinks()
                        .self(SELF_LINK));
    }
}
