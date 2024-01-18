package uk.gov.companieshouse.filinghistory.api.controller;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.mapper.TopLevelMapper;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.ServiceResult;
import uk.gov.companieshouse.filinghistory.api.service.Processor;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = FilingHistoryApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class FilingHistoryControllerIT {

    private static final String FILING_HISTORY_COLLECTION = "company_filing_history";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String PUT_REQUEST_URI = "/company/{company_number/filing-history/{transaction_id}";

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.12");

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Processor filingHistoryProcessor;
    @MockBean
    private TopLevelMapper topLevelMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        when(filingHistoryProcessor.processFilingHistory(any(), any())).thenReturn(ServiceResult.UPSERT_SUCCESSFUL);

        FilingHistoryDocument document = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertNull(document); // Ensure document does not already exist in DB

        InternalFilingHistoryApi request = new InternalFilingHistoryApi();

        // when
        final ResultActions result = mockMvc.perform(put(PUT_REQUEST_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());
    }

}
