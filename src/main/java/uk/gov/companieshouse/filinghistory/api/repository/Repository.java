package uk.gov.companieshouse.filinghistory.api.repository;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.CountOperation;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryListAggregate;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class Repository {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final MongoTemplate mongoTemplate;

    public Repository(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Optional<FilingHistoryListAggregate> findCompanyFilingHistory(String companyNumber,
            int startIndex, int itemsPerPage, List<String> categories) {

        Criteria criteria = Criteria.where("company_number").is(companyNumber);
        if (!categories.isEmpty()) {
            criteria.and("data.category").in(categories);
        }
        MatchOperation match = Aggregation.match(criteria);
        CountOperation count = Aggregation.count().as("count");
        MatchOperation facetMatch = Aggregation.match(new Criteria());

        FacetOperation facet = Aggregation.facet(count).as("total_count")
                .and(facetMatch).as("document_list");
        UnwindOperation unwind = Aggregation.unwind("$total_count", true);

        ProjectionOperation project = Aggregation.project()
                .and(ConditionalOperators.ifNull("$total_count.count").then(0)).as("total_count")
                .andExpression("$document_list").as("document_list");

        Aggregation aggregation = Aggregation.newAggregation(match, facet, unwind, project);

        AggregationResults<FilingHistoryListAggregate> aggregationResults =
                mongoTemplate.aggregate(aggregation, FilingHistoryDocument.class, FilingHistoryListAggregate.class);

        return Optional.ofNullable(aggregationResults.getUniqueMappedResult());
    }

    public Optional<FilingHistoryDocument> findByIdAndCompanyNumber(final String id, final String companyNumber) {
        try {
            Criteria criteria = Criteria.where("_id").is(id);
            if (companyNumber != null) {
                criteria.and("company_number").is(companyNumber);
            }
            Query query = new Query(criteria);

            return Optional.ofNullable(mongoTemplate.findOne(query, FilingHistoryDocument.class));
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB unavailable when finding the document: %s".formatted(ex.getMessage()),
                    DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("MongoDB unavailable when finding the document");
        }
    }

    public Optional<FilingHistoryDocument> findById(final String id) {
        return findByIdAndCompanyNumber(id, null);
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

    public void deleteById(final String id) {
        try {
            mongoTemplate.remove(Query.query(Criteria.where("_id").is(id)), FilingHistoryDocument.class);
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB unavailable when deleting document: %s".formatted(ex.getMessage()),
                    DataMapHolder.getLogMap());
            throw new ServiceUnavailableException("MongoDB unavailable when deleting document");
        }
    }
}
