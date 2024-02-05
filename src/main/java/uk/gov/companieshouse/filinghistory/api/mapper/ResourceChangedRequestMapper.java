package uk.gov.companieshouse.filinghistory.api.mapper;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;

@Component
public class ResourceChangedRequestMapper {

    private final Supplier<String> timestampGenerator;

    public ResourceChangedRequestMapper(Supplier<String> timestampGenerator) {
        this.timestampGenerator = timestampGenerator;
    }

    public ChangedResource mapChangedResource(ResourceChangedRequest request) {
        ChangedResourceEvent event = new ChangedResourceEvent().publishedAt(this.timestampGenerator.get());
        ChangedResource changedResource = new ChangedResource() //NOSONAR
                .resourceUri(String.format("/company/%s/filing-history/%s", request.getCompanyNumber(),
                        request.getTransactionId()))
                .resourceKind("filing-history")
                .event(event)
                .contextId(request.getContextId());

        if (Boolean.TRUE.equals(request.getIsDelete())) {
            event.setType("deleted");
            changedResource.setDeletedData(request.getFilingHistoryData());
        } else {
            event.setType("changed");
        }
        return changedResource;
    }
}
