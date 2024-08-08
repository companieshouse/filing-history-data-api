package uk.gov.companieshouse.filinghistory.api.repository;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.addFields;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.facet;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.ArrayOperators.IndexOfArray.arrayOf;
import static org.springframework.data.mongodb.core.aggregation.ConditionalOperators.ifNull;
import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.exception.BadGatewayException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeleteAggregate;
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
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when finding filing history ids: %s".formatted(ex.getMessage()), ex,
                    DataMapHolder.getLogMap());
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
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when finding full filing history list: %s".formatted(ex.getMessage()), ex,
                    DataMapHolder.getLogMap());
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
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when calculating total count: %s".formatted(ex.getMessage()), ex,
                    DataMapHolder.getLogMap());
            throw new BadGatewayException("MongoDB error when calculating total count", ex);
        }
    }

    public Optional<FilingHistoryDocument> findByIdAndCompanyNumber(final String id, final String companyNumber) {
        try {
            Criteria criteria = Criteria.where("_id").is(id)
                    .and(COMPANY_NUMBER).is(companyNumber);

            Query query = new Query(criteria);

            return Optional.ofNullable(mongoTemplate.findOne(query, FilingHistoryDocument.class));
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when finding the document: %s".formatted(ex.getMessage()), ex,
                    DataMapHolder.getLogMap());
            throw new BadGatewayException("MongoDB error when finding document", ex);
        }
    }

    public Optional<FilingHistoryDeleteAggregate> findByEntityId(final String entityId) {
        try {
            Aggregation aggregation = newAggregation(
                    match(new Criteria()
                            .orOperator(
                                    Criteria.where("_entity_id").is(entityId),
                                    Criteria.where("data.resolutions._entity_id").is(entityId),
                                    Criteria.where("data.annotations._entity_id").is(entityId),
                                    Criteria.where("data.associated_filings._entity_id").is(entityId))),
                    addFields().build()
                            .addField("resolutionIndex",
                                    ifNull(arrayOf("$data.resolutions._entity_id")
                                            .indexOf(entityId))
                                            .then(-1))
                            .addField("annotationIndex",
                                    ifNull(arrayOf("$data.annotations._entity_id")
                                            .indexOf(entityId))
                                            .then(-1))
                            .addField("associatedFilingIndex",
                                    ifNull(arrayOf("$data.associated_filings._entity_id")
                                            .indexOf(entityId))
                                            .then(-1)),
                    project()
                            .andExclude("_id")
                            .andExpression("$resolutionIndex").as("resolution_index")
                            .andExpression("$annotationIndex").as("annotation_index")
                            .andExpression("$associatedFilingIndex").as("associated_filing_index")
                            .andExpression("$$ROOT").as("document"));

            return Optional.ofNullable(
                    mongoTemplate.aggregate(aggregation, FilingHistoryDocument.class,
                            FilingHistoryDeleteAggregate.class).getUniqueMappedResult());
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when trying to retrieve filing history delete document: %s".formatted(
                    ex.getMessage()), ex, DataMapHolder.getLogMap());
            throw new BadGatewayException(
                    "MongoDB error when trying to retrieve filing history delete document", ex);
        }
    }

    public void insert(final FilingHistoryDocument document) {
        try {
            mongoTemplate.insert(document);
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when inserting document: %s".formatted(ex.getMessage()), ex,
                    DataMapHolder.getLogMap());
            throw new BadGatewayException("MongoDB error when inserting document", ex);
        }
    }

    public void update(final FilingHistoryDocument document) {
        try {
            // Initialise version of legacy document
            // Versioning is used to prevent lost updates during concurrent processing
            if (document.getVersion() == null) {
                mongoTemplate.save(new UnversionedFilingHistoryDocument(document));
            } else {
                mongoTemplate.save(document);
            }
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when updating document: %s".formatted(ex.getMessage()), ex,
                    DataMapHolder.getLogMap());
            throw new BadGatewayException("MongoDB error when updating document", ex);
        }
    }

    public void deleteById(final String id) {
        try {
            mongoTemplate.remove(Query.query(Criteria.where("_id").is(id)), FilingHistoryDocument.class);
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when deleting document: %s".formatted(ex.getMessage()), ex,
                    DataMapHolder.getLogMap());
            throw new BadGatewayException("MongoDB error when deleting document", ex);
        }
    }
}
