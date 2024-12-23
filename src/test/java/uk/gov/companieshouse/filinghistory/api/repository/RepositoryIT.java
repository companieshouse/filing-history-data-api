package uk.gov.companieshouse.filinghistory.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import uk.gov.companieshouse.filinghistory.api.exception.BadGatewayException;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryIds;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryLinks;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryListAggregate;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryOriginalValues;

@Testcontainers
@SpringBootTest
class RepositoryIT {

    private static final String FILING_HISTORY_COLLECTION = "company_filing_history";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String TRANSACTION_ID_TWO = "transactionIdTwo";
    private static final String ENTITY_ID = "1234567890";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String EXISTING_DELTA_AT = "20140815230459600643";
    private static final int START_INDEX = 0;
    private static final int DEFAULT_ITEMS_PER_PAGE = 25;
    private static final String OFFICERS_CATEGORY = "officers";
    private static final int TOTAL_RESULTS_NUMBER = 55;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.12");

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private Repository repository;

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
    void testMappingsFromMongoToDocument() throws IOException {
        // given
        final String jsonToInsert = IOUtils.resourceToString("/mongo_docs/filing-history-document.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<entity_id>", ENTITY_ID)
                .replaceAll("<delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<category>", OFFICERS_CATEGORY);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);

        final FilingHistoryDocument expectedDocument = getFilingHistoryDocument(TRANSACTION_ID);

        // when
        final Optional<FilingHistoryDocument> actualDocument = repository.findByIdAndCompanyNumber(TRANSACTION_ID,
                COMPANY_NUMBER);

        // then
        assertTrue(actualDocument.isPresent());
        assertEquals(expectedDocument, actualDocument.get());
    }

    @Test
    void testAggregationQueriesToFindTwoDocuments() throws IOException {
        // given
        final String jsonToInsert = IOUtils.resourceToString("/mongo_docs/filing-history-document.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<entity_id>", ENTITY_ID)
                .replaceAll("<delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<category>", OFFICERS_CATEGORY);
        final String jsonToInsertTwo = IOUtils.resourceToString("/mongo_docs/filing-history-document.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID_TWO)
                .replaceAll("<entity_id>", ENTITY_ID)
                .replaceAll("<delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<category>", OFFICERS_CATEGORY);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);
        mongoTemplate.insert(Document.parse(jsonToInsertTwo), FILING_HISTORY_COLLECTION);

        final FilingHistoryListAggregate expected = getFilingHistoryListAggregate();

        // when
        final FilingHistoryIds listOfFilingHistoryIds = repository.findCompanyFilingHistoryIds(COMPANY_NUMBER,
                START_INDEX, DEFAULT_ITEMS_PER_PAGE, List.of());
        final List<FilingHistoryDocument> documentList = repository.findFullFilingHistoryDocuments(
                listOfFilingHistoryIds.getIds());
        final long totalCount = repository.countTotal(COMPANY_NUMBER, List.of());

        // then
        assertEquals(expected, new FilingHistoryListAggregate().documentList(documentList).totalCount(totalCount));
    }

    @Test
    void testAggregationQueriesToFindOneDocumentWhenCategoryFilter() throws IOException {
        // given
        final String jsonToInsert = IOUtils.resourceToString("/mongo_docs/filing-history-document.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<entity_id>", ENTITY_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<category>", OFFICERS_CATEGORY);
        final String jsonToInsertTwo = IOUtils.resourceToString("/mongo_docs/filing-history-document.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID_TWO)
                .replaceAll("<entity_id>", ENTITY_ID)
                .replaceAll("<delta_at>", EXISTING_DELTA_AT)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<category>", "incorporation");
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);
        mongoTemplate.insert(Document.parse(jsonToInsertTwo), FILING_HISTORY_COLLECTION);

        final FilingHistoryListAggregate expected = getFilingHistoryListAggregateOneDocument();
        expected.getDocumentList().getFirst().getData().category("incorporation");

        // when
        final FilingHistoryIds listOfFilingHistoryIds = repository.findCompanyFilingHistoryIds(COMPANY_NUMBER,
                START_INDEX, DEFAULT_ITEMS_PER_PAGE, List.of("incorporation"));
        final List<FilingHistoryDocument> documentList = repository.findFullFilingHistoryDocuments(
                listOfFilingHistoryIds.getIds());
        final long totalCount = repository.countTotal(COMPANY_NUMBER, List.of("incorporation"));

        // then
        assertEquals(expected, new FilingHistoryListAggregate().documentList(documentList).totalCount(totalCount));
    }

    @Test
    void testAggregationQueriesToFindDocumentsWithLargeStartIndex() {
        for (int i = 0; i < TOTAL_RESULTS_NUMBER; i++) {
            FilingHistoryDocument filingHistoryDocument = new FilingHistoryDocument();
            filingHistoryDocument.transactionId(TRANSACTION_ID + i);
            filingHistoryDocument.companyNumber(COMPANY_NUMBER);
            mongoTemplate.insert(filingHistoryDocument);
        }

        // when
        final FilingHistoryIds listOfFilingHistoryIds = repository.findCompanyFilingHistoryIds(COMPANY_NUMBER,
                20, DEFAULT_ITEMS_PER_PAGE, List.of());
        final List<FilingHistoryDocument> documentList = repository.findFullFilingHistoryDocuments(
                listOfFilingHistoryIds.getIds());
        final long totalCount = repository.countTotal(COMPANY_NUMBER, List.of());
        FilingHistoryListAggregate actual = new FilingHistoryListAggregate().documentList(documentList)
                .totalCount(totalCount);

        // then
        assertEquals(TOTAL_RESULTS_NUMBER, actual.getTotalCount());
        assertEquals(TRANSACTION_ID + 20, actual.getDocumentList().getFirst().getTransactionId());
        assertEquals(DEFAULT_ITEMS_PER_PAGE, actual.getDocumentList().size());
    }

    @Test
    void testAggregationQueriesToFindDocumentsWithStartIndexHigherThanItemsPerPage() {
        for (int i = 0; i < TOTAL_RESULTS_NUMBER; i++) {
            FilingHistoryDocument filingHistoryDocument = new FilingHistoryDocument();
            filingHistoryDocument.transactionId(TRANSACTION_ID + i);
            filingHistoryDocument.companyNumber(COMPANY_NUMBER);
            mongoTemplate.insert(filingHistoryDocument);
        }

        // when
        final FilingHistoryIds listOfFilingHistoryIds = repository.findCompanyFilingHistoryIds(COMPANY_NUMBER,
                60, DEFAULT_ITEMS_PER_PAGE, List.of());
        final List<FilingHistoryDocument> documentList = repository.findFullFilingHistoryDocuments(
                listOfFilingHistoryIds.getIds());
        final long totalCount = repository.countTotal(COMPANY_NUMBER, List.of());
        FilingHistoryListAggregate actual = new FilingHistoryListAggregate().documentList(documentList)
                .totalCount(totalCount);

        // then
        assertEquals(TOTAL_RESULTS_NUMBER, actual.getTotalCount());
        assertTrue(actual.getDocumentList().isEmpty());
    }

    @Test
    void testAggregateQueriesWhenNoDocumentsInDatabase() {
        // given

        // when
        final FilingHistoryIds listOfFilingHistoryIds = repository.findCompanyFilingHistoryIds(COMPANY_NUMBER,
                START_INDEX, DEFAULT_ITEMS_PER_PAGE, List.of());
        final List<FilingHistoryDocument> documentList = repository.findFullFilingHistoryDocuments(
                listOfFilingHistoryIds.getIds());
        final long totalCount = repository.countTotal(COMPANY_NUMBER, List.of());
        FilingHistoryListAggregate actual = new FilingHistoryListAggregate().documentList(documentList)
                .totalCount(totalCount);

        // then
        assertEquals(0, actual.getTotalCount());
        assertTrue(actual.getDocumentList().isEmpty());
    }

    @Test
    void testAggregationQueriesToFindDocumentsWithSortingOnDate() {
        for (int i = 0; i < TOTAL_RESULTS_NUMBER; i++) {
            FilingHistoryDocument filingHistoryDocument = new FilingHistoryDocument();
            filingHistoryDocument.transactionId(TRANSACTION_ID + i);
            filingHistoryDocument.companyNumber(COMPANY_NUMBER);
            filingHistoryDocument.data(new FilingHistoryData().date(Instant.now()));
            mongoTemplate.insert(filingHistoryDocument);
        }

        // when
        final FilingHistoryIds listOfFilingHistoryIds = repository.findCompanyFilingHistoryIds(COMPANY_NUMBER,
                START_INDEX, DEFAULT_ITEMS_PER_PAGE, List.of());
        final List<FilingHistoryDocument> documentList = repository.findFullFilingHistoryDocuments(
                listOfFilingHistoryIds.getIds());
        final long totalCount = repository.countTotal(COMPANY_NUMBER, List.of());
        FilingHistoryListAggregate actual = new FilingHistoryListAggregate().documentList(documentList)
                .totalCount(totalCount);

        // then
        assertEquals(TOTAL_RESULTS_NUMBER, actual.getTotalCount());
        assertEquals("transactionId54", actual.getDocumentList().getFirst().getTransactionId());
    }

    @Test
    void testAggregationQueriesToFindDocumentsWithSortingAndPaginationOnVeryLargeDataSet() {
        final int DOC_COUNT = 300_000; // Reduced to 300_000 as versioning increased memory usage
        List<FilingHistoryDocument> documentList = new ArrayList<>();
        for (int i = 0; i < DOC_COUNT; i++) {
            FilingHistoryDocument filingHistoryDocument = new FilingHistoryDocument();
            filingHistoryDocument.transactionId(TRANSACTION_ID + i);
            filingHistoryDocument.companyNumber(COMPANY_NUMBER);
            filingHistoryDocument.data(new FilingHistoryData().date(Instant.now().plusMillis(i)));
            documentList.add(filingHistoryDocument);
        }
        mongoTemplate.insert(documentList, FILING_HISTORY_COLLECTION);

        // when
        final FilingHistoryIds listOfFilingHistoryIds = repository.findCompanyFilingHistoryIds(COMPANY_NUMBER,
                DOC_COUNT - 26, DEFAULT_ITEMS_PER_PAGE, List.of());
        final List<FilingHistoryDocument> documentListReturned = repository.findFullFilingHistoryDocuments(
                listOfFilingHistoryIds.getIds());
        final long totalCount = repository.countTotal(COMPANY_NUMBER, List.of());
        FilingHistoryListAggregate actual = new FilingHistoryListAggregate().documentList(documentListReturned)
                .totalCount(totalCount);

        // then
        assertEquals(DOC_COUNT, actual.getTotalCount());
        assertEquals("transactionId25", actual.getDocumentList().getFirst().getTransactionId());
    }

    @Test
    void testInvalidCompanyNumber() throws IOException {
        // given
        final String jsonToInsert = IOUtils.resourceToString("/mongo_docs/filing-history-document.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<category>", OFFICERS_CATEGORY);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);

        // when
        final Optional<FilingHistoryDocument> actualDocument = repository.findByIdAndCompanyNumber(TRANSACTION_ID,
                "87654321");

        // then
        assertTrue(actualDocument.isEmpty());
    }

    @Test
    void shouldSuccessfullyInsertDocument() {
        // given
        FilingHistoryDocument document = getFilingHistoryDocument(TRANSACTION_ID);

        // when
        repository.insert(document);

        // then
        FilingHistoryDocument actual = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertEquals(document, actual);
    }

    @Test
    void shouldCatchDuplicateKeyExceptionAndThrowBadGatewayExceptionWhenInsertingSameDocTwice() {
        // given
        FilingHistoryDocument document = getFilingHistoryDocument(TRANSACTION_ID);
        repository.insert(document);

        // when
        Executable executable = () -> repository.insert(document);

        // then
        assertThrows(BadGatewayException.class, executable);
    }

    @Test
    void shouldSuccessfullyUpdateDocumentAndIncrementVersion() {
        // given
        FilingHistoryDocument document = getFilingHistoryDocument(TRANSACTION_ID);
        mongoTemplate.insert(document);

        FilingHistoryDocument expected = getFilingHistoryDocument(TRANSACTION_ID)
                .version(1);

        // when
        repository.update(document);

        // then
        FilingHistoryDocument actual = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertEquals(expected, actual);
    }

    @Test
    void shouldCatchOptimisticLockingFailureExceptionAndThrowBadGatewayExceptionWhenVersionsOfUpdateAreDifferent() {
        // given
        FilingHistoryDocument existingDoc = new FilingHistoryDocument().transactionId(TRANSACTION_ID);
        mongoTemplate.insert(existingDoc);

        FilingHistoryDocument updateDoc = new FilingHistoryDocument().transactionId(TRANSACTION_ID).version(0);
        repository.update(updateDoc);

        // when
        FilingHistoryDocument updateDocDifferentVersion = new FilingHistoryDocument().transactionId(TRANSACTION_ID)
                .version(0);
        Executable executable = () -> repository.update(updateDocDifferentVersion);

        // then
        assertThrows(BadGatewayException.class, executable);
    }

    @Test
    void shouldSuccessfullyDeleteDocumentById() throws IOException {
        // given
        final String jsonToInsert = IOUtils.resourceToString("/mongo_docs/filing-history-document.json",
                        StandardCharsets.UTF_8)
                .replaceAll("<id>", TRANSACTION_ID)
                .replaceAll("<company_number>", COMPANY_NUMBER)
                .replaceAll("<category>", OFFICERS_CATEGORY);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);

        // when
        repository.deleteById(TRANSACTION_ID);

        // then
        FilingHistoryDocument actual = mongoTemplate.findById(TRANSACTION_ID, FilingHistoryDocument.class);
        assertNull(actual);
    }

    private static FilingHistoryListAggregate getFilingHistoryListAggregateOneDocument() {
        return new FilingHistoryListAggregate()
                .totalCount(1)
                .documentList(
                        List.of(
                                getFilingHistoryDocument(TRANSACTION_ID_TWO)));
    }

    private static FilingHistoryListAggregate getFilingHistoryListAggregate() {
        return new FilingHistoryListAggregate()
                .totalCount(2)
                .documentList(
                        List.of(
                                getFilingHistoryDocument(TRANSACTION_ID),
                                getFilingHistoryDocument(TRANSACTION_ID_TWO)));
    }

    private static FilingHistoryDocument getFilingHistoryDocument(final String transactionId) {
        return new FilingHistoryDocument()
                .transactionId(transactionId)
                .companyNumber(COMPANY_NUMBER)
                .data(new FilingHistoryData()
                        .actionDate(Instant.parse("2014-08-29T00:00:00.000Z"))
                        .category(OFFICERS_CATEGORY)
                        .type("TM01")
                        .description("termination-director-company-with-name-termination-date")
                        .subcategory("termination")
                        .date(Instant.parse("2014-09-15T23:21:18.000Z"))
                        .descriptionValues(new FilingHistoryDescriptionValues()
                                .terminationDate(Instant.parse("2014-08-29T00:00:00.000Z"))
                                .officerName("John Tester"))
                        .annotations(List.of(new FilingHistoryAnnotation()
                                .annotation(
                                        "Clarification This document was second filed with the CH04 registered on 26/11/2011")
                                .category("annotation")
                                .date(Instant.parse("2011-11-26T11:27:55.000Z"))
                                .description("annotation")
                                .descriptionValues(new FilingHistoryDescriptionValues()
                                        .description(
                                                "Clarification This document was second filed with the CH04 registered on 26/11/2011"))
                                .type("ANNOTATION")
                                .entityId("2234567890")
                                .deltaAt("20140815230459600643")))
                        .links(new FilingHistoryLinks()
                                .documentMetadata("/document/C1_z-KlM567zSgwJz8uN-UZ3_xnGfCljj3k7L69LxwA")
                                .self("/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, transactionId)))
                        .pages(1))
                .barcode("X4BI89B6")
                .deltaAt("20140815230459600643")
                .entityId("1234567890")
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(Instant.parse("2014-09-17T18:52:08.001Z"))
                        .by("5419d856b6a59f32b7684d0e"))
                .created(new FilingHistoryDeltaTimestamp()
                        .at(Instant.parse("2014-09-14T18:52:08.001Z"))
                        .by("5419d856b6a59f32b7684dE4"))
                .originalValues(new FilingHistoryOriginalValues()
                        .officerName("John Tester")
                        .resignationDate("29/08/2014"))
                .originalDescription("Appointment Terminated, Director john tester")
                .documentId("000X4BI89B65846");
    }
}
