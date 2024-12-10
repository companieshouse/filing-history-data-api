package uk.gov.companieshouse.filinghistory.api.repository;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.addFields;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.facet;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.ArrayOperators.IndexOfArray.arrayOf;
import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.exception.BadGatewayException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryIds;
import uk.gov.companieshouse.filinghistory.api.model.mongo.UnversionedFilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class Repository {

    private static final String COMPANY_NUMBER = "company_number";
    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final MongoTemplate mongoTemplate;

    public Repository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public FilingHistoryIds findCompanyFilingHistoryIds(String companyNumber,
            int startIndex, int itemsPerPage, List<String> categoryList) {
        try {
            Criteria criteria = Criteria.where(COMPANY_NUMBER).is(companyNumber);
            if (!categoryList.isEmpty()) {
                criteria.and("data.category").in(categoryList);
            }
            Aggregation aggregation = newAggregation(
                    match(criteria),
                    facet(sort(Direction.DESC, "data.date")).as("ids"),
                    project().andExpression("$ids._id").slice(itemsPerPage, startIndex).as("ids"));

            return mongoTemplate.aggregate(aggregation, FilingHistoryDocument.class, FilingHistoryIds.class)
                    .getUniqueMappedResult();
        } catch (TransientDataAccessException ex) {
            LOGGER.info("Recoverable MongoDB error when finding filing history ids", DataMapHolder.getLogMap());
            throw new BadGatewayException("Recoverable MongoDB error when finding filing history ids", ex);
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when finding filing history ids", ex, DataMapHolder.getLogMap());
            throw new BadGatewayException("MongoDB error when finding filing history ids", ex);
        }
    }

    public List<FilingHistoryDocument> findFullFilingHistoryDocuments(List<String> filingHistoryIds) {
        try {
            Criteria criteria = Criteria.where("_id").in(filingHistoryIds);
            Aggregation aggregation = newAggregation(
                    match(criteria),
                    addFields().addFieldWithValue("__sort_order__", arrayOf(filingHistoryIds).indexOf("$_id")).build(),
                    sort(Direction.ASC, "__sort_order__")
            );
            return mongoTemplate.aggregate(aggregation, FilingHistoryDocument.class, FilingHistoryDocument.class)
                    .getMappedResults();
        } catch (TransientDataAccessException ex) {
            LOGGER.info("Recoverable MongoDB error when finding full filing history list", DataMapHolder.getLogMap());
            throw new BadGatewayException("Recoverable MongoDB error when finding full filing history list", ex);
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when finding full filing history list", ex, DataMapHolder.getLogMap());
            throw new BadGatewayException("MongoDB error when finding full filing history list", ex);
        }
    }

    public long countTotal(String companyNumber, List<String> categoryList) {
        try {
            Criteria criteria = Criteria.where(COMPANY_NUMBER).is(companyNumber);
            if (!categoryList.isEmpty()) {
                criteria.and("data.category").in(categoryList);
            }

            return mongoTemplate.count(Query.query(criteria), FilingHistoryDocument.class);
        } catch (TransientDataAccessException ex) {
            LOGGER.info("Recoverable MongoDB error when calculating total count", DataMapHolder.getLogMap());
            throw new BadGatewayException("Recoverable MongoDB error when calculating total count", ex);
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when calculating total count", ex, DataMapHolder.getLogMap());
            throw new BadGatewayException("MongoDB error when calculating total count", ex);
        }
    }

    public Optional<FilingHistoryDocument> findByIdAndCompanyNumber(final String id, final String companyNumber) {
        try {
            Criteria criteria = Criteria.where("_id").is(id)
                    .and(COMPANY_NUMBER).is(companyNumber);

            Query query = new Query(criteria);

            return Optional.ofNullable(mongoTemplate.findOne(query, FilingHistoryDocument.class));
        } catch (TransientDataAccessException ex) {
            LOGGER.info("Recoverable MongoDB error when finding document", DataMapHolder.getLogMap());
            throw new BadGatewayException("Recoverable MongoDB error when finding document", ex);
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when finding document", ex, DataMapHolder.getLogMap());
            throw new BadGatewayException("MongoDB error when finding document", ex);
        }
    }

    public void insert(final FilingHistoryDocument document) {
        try {
            mongoTemplate.insert(document);
        } catch (DuplicateKeyException ex) {
            LOGGER.info("Failed insert: Tried inserting record with duplicate id", DataMapHolder.getLogMap());
            throw new BadGatewayException("Failed insert: Tried inserting record with duplicate id", ex);
        } catch (TransientDataAccessException ex) {
            LOGGER.info("Recoverable MongoDB error when inserting document", DataMapHolder.getLogMap());
            throw new BadGatewayException("Recoverable MongoDB error when inserting document", ex);
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when inserting document", ex, DataMapHolder.getLogMap());
            throw new BadGatewayException("MongoDB error when inserting document", ex);
        }
    }

    public void update(final FilingHistoryDocument document) {
        try {
            // Initialise version of legacy document
            // Versioning is used to prevent lost updates during concurrent processing
            if (document.getVersion() == null) {
                LOGGER.info("Initialising version of legacy document", DataMapHolder.getLogMap());
                mongoTemplate.save(new UnversionedFilingHistoryDocument(document));
            } else {
                mongoTemplate.save(document);
            }
        } catch (OptimisticLockingFailureException ex) {
            LOGGER.info("Failed update: Document not most recent version", DataMapHolder.getLogMap());
            throw new BadGatewayException("Failed update: Document not most recent version", ex);
        } catch (TransientDataAccessException ex) {
            LOGGER.info("Recoverable MongoDB error when updating document", DataMapHolder.getLogMap());
            throw new BadGatewayException("Recoverable MongoDB error when updating document", ex);
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when updating document", ex, DataMapHolder.getLogMap());
            throw new BadGatewayException("MongoDB error when updating document", ex);
        }
    }

    public void deleteById(final String id) {
        try {
            mongoTemplate.remove(Query.query(Criteria.where("_id").is(id)), FilingHistoryDocument.class);
        } catch (TransientDataAccessException ex) {
            LOGGER.info("Recoverable MongoDB error when deleting document", DataMapHolder.getLogMap());
            throw new BadGatewayException("Recoverable MongoDB error when deleting document", ex);
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when deleting document", ex, DataMapHolder.getLogMap());
            throw new BadGatewayException("MongoDB error when deleting document", ex);
        }
    }
}
