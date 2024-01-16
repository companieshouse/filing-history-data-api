package uk.gov.companieshouse.filinghistory.api.repository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@Component
public class Repository {

    private final MongoTemplate mongoTemplate;

    public Repository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public FilingHistoryDocument findById(String id) {
        return mongoTemplate.findById(id, FilingHistoryDocument.class);
    }
}
