package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import java.time.Instant;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.mapper.get.ItemGetResponseMapper;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@Component
public class ResourceChangedRequestMapper {

    private final ItemGetResponseMapper itemGetResponseMapper;
    private final Supplier<Instant> instantSupplier;

    public ResourceChangedRequestMapper(ItemGetResponseMapper itemGetResponseMapper,
            Supplier<Instant> instantSupplier) {
        this.itemGetResponseMapper = itemGetResponseMapper;
        this.instantSupplier = instantSupplier;
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
            changedResource.setDeletedData(itemGetResponseMapper.mapFilingHistoryItem(document));
        } else {
            event.setType("changed");
        }
        return changedResource;
    }
}