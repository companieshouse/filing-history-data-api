package uk.gov.companieshouse.filinghistory.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
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
    private static final String SELF_LINK = "/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID);
    private static final String ENTITY_ID = "1234567890";
    private static final String CHILD_ENTITY_ID = "2234567890";
    private static final String BARCODE = "X4BI89B6";
    private static final String NEWEST_REQUEST_DELTA_AT = "20140916230459600643";
    private static final String STALE_REQUEST_DELTA_AT = "20130615185208001000";
    private static final String EXISTING_DELTA_AT = "20140815230459600643";
    private static final Instant UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);
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
}
