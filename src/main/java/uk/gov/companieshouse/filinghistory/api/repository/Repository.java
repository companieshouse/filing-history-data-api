package uk.gov.companieshouse.filinghistory.api.repository;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class Repository {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final MongoTemplate mongoTemplate;

    public Repository(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Optional<FilingHistoryDocument> findById(final String id) {
        try {
            return Optional.ofNullable(mongoTemplate.findById(id, FilingHistoryDocument.class));
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB unavailable when finding the document: %s".formatted(ex.getMessage()),
                    DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("MongoDB unavailable when finding the document");
        }
    }

    public void save(final FilingHistoryDocument document) {
        try {
            mongoTemplate.save(document);
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB unavailable when saving the document: %s".formatted(ex.getMessage()),
                    DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("MongoDB unavailable when saving the document");
        }
    }

    public void rollBackToOriginalState(FilingHistoryDocument existingDocument, final String transactionId) {
        try {
            if (existingDocument != null) {
                mongoTemplate.save(existingDocument);
            } else {
                mongoTemplate.remove(
                        Query.query(Criteria.where("_id").is(transactionId)),
                        FilingHistoryDocument.class);
            }
        } catch (DataAccessException ex) {
            throw new ServiceUnavailableException("MongoDB unavailable when rolling back document");
        }
    }
}
