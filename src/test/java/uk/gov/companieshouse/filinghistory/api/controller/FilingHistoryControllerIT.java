package uk.gov.companieshouse.filinghistory.api.controller;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.mapper.TopLevelTransactionMapper;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryLinks;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryOriginalValues;
import uk.gov.companieshouse.filinghistory.api.model.ServiceResult;
import uk.gov.companieshouse.filinghistory.api.service.Processor;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = FilingHistoryApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class FilingHistoryControllerIT {

    private static final String FILING_HISTORY_COLLECTION = "company_filing_history";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String PUT_REQUEST_URI = "/company/{company_number}/filing-history/{transaction_id}";
    private static final String ENTITY_ID = "1234567890";
    private static final String DOCUMENT_ID = "documentId";
    private static final String BARCODE = "barcode";
    public static final String NEWEST_REQUEST_DELTA_AT = "20151025185208001000";
    public static final String STALE_REQUEST_DELTA_AT = "20130615185208001000";
    private static final String UPDATED_BY = "84746291";
    private static final String TM01_TYPE = "TM01";
    private static final LocalDate DATE = LocalDate.of(2015, 1, 25);
    private static final LocalDate ACTION_AND_TERMINATION_DATE_AS_LOCAL_DATE = LocalDate.of(2015, 2, 26);
    private static final Instant ACTION_AND_TERMINATION_DATE_AS_INSTANT = Instant.from(
            ACTION_AND_TERMINATION_DATE_AS_LOCAL_DATE.atStartOfDay(UTC));

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.12");

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        final FilingHistoryDocument expectedDocument = getExpectedFilingHistoryDocument();
        final InternalFilingHistoryApi request = buildPutRequestBody();

        // when
        final ResultActions result = mockMvc.perform(put(PUT_REQUEST_URI, COMPANY_NUMBER, TRANSACTION_ID)
                .header("ERIC-Identity", "123")
                .header("ERIC-Identity-Type", "key")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk());
        result.andExpect(MockMvcResultMatchers.header().string(LOCATION, "/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID)));

        FilingHistoryDocument actualDocument = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertEquals(expectedDocument, actualDocument);
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
                .documentId("000X4BI89B65846")
                .deltaAt(NEWEST_REQUEST_DELTA_AT)
                .originalDescription("original description")
                .originalValues(new InternalDataOriginalValues()
                        .officerName("John Tester")
                        .resignationDate("29/08/2014"))
                .parentEntityId("parent_entity_id")
                .updatedBy(UPDATED_BY);
    }

    private static ExternalData buildExternalData() {
        return new ExternalData()
                .transactionId(TRANSACTION_ID)
                .barcode("X4BI89B6")
                .type(TM01_TYPE)
                .date(OffsetDateTime.of(2014, 9, 15, 23, 21, 18, 0, UTC))
                .category(ExternalData.CategoryEnum.OFFICERS)
                .annotations(null)
                .subcategory(ExternalData.SubcategoryEnum.TERMINATION)
                .description("termination-director-company-with-name-termination-date")
                .descriptionValues(new FilingHistoryItemDataDescriptionValues()
                        .officerName("John Tester")
                        .terminationDate(DATE))
                .pages(1)
                .actionDate(LocalDate.of(2014, 8, 29))
                .paperFiled(true)
                .links(new FilingHistoryItemDataLinks()
                        .self("/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID))
                        .documentMetadata("metadata"));
    }

    private static FilingHistoryDocument getExpectedFilingHistoryDocument() {
        return new FilingHistoryDocument()
                .transactionId(TRANSACTION_ID)
                .companyNumber(COMPANY_NUMBER)
                .data(new FilingHistoryData()
                        .actionDate(Instant.parse("2014-08-29T00:00:00.000Z"))
                        .category("officers")
                        .type("TM01")
                        .description("termination-director-company-with-name-termination-date")
                        .subcategory("termination")
                        .date(Instant.parse("2014-09-15T23:21:18.000Z"))
                        .paperFiled(true)
                        .descriptionValues(new FilingHistoryDescriptionValues()
                                .terminationDate(Instant.parse("2014-08-29T00:00:00.000Z"))
                                .officerName("John Tester"))
                        .links(new FilingHistoryLinks()
                                .documentMetadata("/document/C1_z-KlM567zSgwJz8uN-UZ3_xnGfCljj3k7L69LxwA")
                                .self("/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID)))
                        .pages(1))
                .barcode("X4BI89B6")
                .deltaAt("20140916230459600643")
                .entityId("1234567890")
                .updatedAt(Instant.parse("2014-09-17T18:52:08.001Z"))
                .updatedBy("5419d856b6a59f32b7684d0e")
                .originalValues(new FilingHistoryOriginalValues()
                        .officerName("John Tester")
                        .resignationDate("29/08/2014"))
                .originalDescription("Appointment Terminated, Director john tester")
                .documentId("000X4BI89B65846");
    }
}
