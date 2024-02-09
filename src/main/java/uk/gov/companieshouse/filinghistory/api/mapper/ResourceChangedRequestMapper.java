package uk.gov.companieshouse.filinghistory.api.mapper;

import java.time.Instant;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;

@Component
public class ResourceChangedRequestMapper {

    private final Supplier<Instant> instantSupplier;

    public ResourceChangedRequestMapper(Supplier<Instant> instantSupplier) {
        this.instantSupplier = instantSupplier;
    }

    public ChangedResource mapChangedResource(ResourceChangedRequest request) {
        ChangedResourceEvent event = new ChangedResourceEvent().publishedAt(instantSupplier.get().toString());
        ChangedResource changedResource = new ChangedResource()
                .resourceUri(String.format("/company/%s/filing-history/%s", request.companyNumber(),
                        request.transactionId()))
                .resourceKind("filing-history")
                .event(event)
                .contextId(request.contextId());

        if (Boolean.TRUE.equals(request.isDelete())) {
            event.setType("deleted");
            changedResource.setDeletedData(request.filingHistoryData());
        } else {
            event.setType("changed");
        }
        return changedResource;
    }
}