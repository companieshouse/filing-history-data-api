package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import java.time.Instant;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.filinghistory.api.mapper.get.ItemGetResponseMapper;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;

@Component
public class ResourceChangedRequestMapper {

    private final ItemGetResponseMapper itemGetResponseMapper;
    private final Supplier<Instant> instantSupplier;

    public ResourceChangedRequestMapper(ItemGetResponseMapper itemGetResponseMapper, Supplier<Instant> instantSupplier) {
        this.itemGetResponseMapper = itemGetResponseMapper;
        this.instantSupplier = instantSupplier;
    }

    public ChangedResource mapChangedResource(ResourceChangedRequest request) {
        ChangedResourceEvent event = new ChangedResourceEvent().publishedAt(instantSupplier.get().toString());
        ChangedResource changedResource = new ChangedResource()
                .resourceUri(String.format("/company/%s/filing-history/%s", request.filingHistoryDocument().getCompanyNumber(),
                        request.filingHistoryDocument().getTransactionId()))
                .resourceKind("filing-history")
                .event(event)
                .contextId(request.contextId());

        if (request.isDelete()) {
            event.setType("deleted");
            changedResource.setDeletedData(itemGetResponseMapper.mapFilingHistoryItem(request.filingHistoryDocument()));
        } else {
            event.setType("changed");
        }
        return changedResource;
    }
}