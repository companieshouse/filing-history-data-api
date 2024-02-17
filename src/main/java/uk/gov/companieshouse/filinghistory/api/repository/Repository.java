package uk.gov.companieshouse.filinghistory.api.repository;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
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
            LOGGER.error("MongoDB was unavailable when attempting to find the document", DataMapHolder.getLogMap());
            throw new ServiceUnavailableException(
                    "MongoDB was unavailable when attempting to find the document: %s".formatted(ex.getMessage()));
        }
    }

    public void save(final FilingHistoryDocument document) {
        try {
            mongoTemplate.save(document);
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB was unavailable when attempting to save the document", DataMapHolder.getLogMap());
            throw new ServiceUnavailableException(
                    "MongoDB was unavailable when attempting to save the document: %s".formatted(ex.getMessage()));
        }
    }
}
