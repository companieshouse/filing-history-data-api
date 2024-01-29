package uk.gov.companieshouse.filinghistory.api.repository;

import java.util.Optional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@Component
public class Repository {

    private final MongoTemplate mongoTemplate;

    public Repository(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Optional<FilingHistoryDocument> findById(final String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, FilingHistoryDocument.class));
    }

    public void save(final FilingHistoryDocument document) {
        mongoTemplate.save(document);
    }
}
