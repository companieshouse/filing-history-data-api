package uk.gov.companieshouse.filinghistory.api.repository;

import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import uk.gov.companieshouse.filinghistory.api.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
class RepositoryIT {

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5");
    private static final String FILING_HISTORY_COLLECTION = "company_filing_history";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String COMPANY_NUMBER = "12345678";

    @Autowired
    private Repository repository;

    @BeforeAll
    static void start() throws IOException {
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        MongoTemplate mongoTemplate = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoDBContainer.getReplicaSetUrl()));
        mongoTemplate.createCollection(FILING_HISTORY_COLLECTION);
        final String jsonToInsert = IOUtils.resourceToString("/filing-history-document.json", StandardCharsets.UTF_8)
                        .replaceAll("<id>", TRANSACTION_ID)
                        .replaceAll("<company_number>", COMPANY_NUMBER);
        mongoTemplate.insert(Document.parse(jsonToInsert), FILING_HISTORY_COLLECTION);
    }

    @Test
    void testMappingsFromMongoToDocument() {
        // given
        final FilingHistoryDocument expectedDocument = new FilingHistoryDocument()
                .transactionId(TRANSACTION_ID)
                .companyNumber(COMPANY_NUMBER)
                .data(new FilingHistoryData()
                        .actionDate(Instant.parse("2014-08-29T00:00:00.000Z"))
                        .category("officers")
                        .type("TM01")
                        .description("termination-director-company-with-name-termination-date")
                        .subcategory("termination")
                        .date(Instant.parse("2014-09-15T23:21:18.000Z"))
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

        // when
        final FilingHistoryDocument actualDocument = repository.findById(TRANSACTION_ID);

        // then
        assertEquals(expectedDocument, actualDocument);
    }
}
