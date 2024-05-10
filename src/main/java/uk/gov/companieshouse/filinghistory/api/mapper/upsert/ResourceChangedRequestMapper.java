package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.filinghistory.api.exception.InternalServerErrorException;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.mapper.get.ItemGetResponseMapper;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class ResourceChangedRequestMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String SERDES_ERROR_MSG = "Serialisation/deserialisation failed when mapping changed resource";

    private final ItemGetResponseMapper itemGetResponseMapper;
    private final Supplier<Instant> instantSupplier;
    private final ObjectMapper nullCleaningObjectMapper;

    public ResourceChangedRequestMapper(ItemGetResponseMapper itemGetResponseMapper,
                                        Supplier<Instant> instantSupplier, ObjectMapper nullCleaningObjectMapper) {
        this.itemGetResponseMapper = itemGetResponseMapper;
        this.instantSupplier = instantSupplier;
        this.nullCleaningObjectMapper = nullCleaningObjectMapper;
    }

    public ChangedResource mapChangedResource(ResourceChangedRequest request) {
        FilingHistoryDocument document = request.filingHistoryDocument();
        ChangedResourceEvent event = new ChangedResourceEvent().publishedAt(instantSupplier.get().toString());
        ChangedResource changedResource = new ChangedResource()
                .resourceUri("/company/%s/filing-history/%s".formatted(document.getCompanyNumber(),
                        document.getTransactionId()))
                .resourceKind("filing-history")
                .event(event)
                .contextId(DataMapHolder.getRequestId());

        if (request.isDelete()) {
            event.setType("deleted");
            try {
                final String serialisedDeletedData =
                        nullCleaningObjectMapper.writeValueAsString(itemGetResponseMapper.mapFilingHistoryItem(document));
                changedResource.setDeletedData(nullCleaningObjectMapper.readValue(serialisedDeletedData, Object.class));
            } catch (JsonProcessingException ex) {
                LOGGER.error(SERDES_ERROR_MSG, ex, DataMapHolder.getLogMap());
                throw new InternalServerErrorException(SERDES_ERROR_MSG);
            }
        } else {
            event.setType("changed");
        }
        return changedResource;
    }
}