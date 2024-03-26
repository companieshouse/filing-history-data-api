package uk.gov.companieshouse.filinghistory.api.mapper.get;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.Links;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryLinks;

@Component
public class LinksGetResponseMapper {

    private final String documentApiUrl;

    public LinksGetResponseMapper(@Value("${api.document-api-url}") String documentApiUrl) {
        this.documentApiUrl = documentApiUrl;
    }

    public Links map(FilingHistoryLinks links) {
        final String metadataLink = links.getDocumentMetadata() == null ?
                null : "%s%s".formatted(documentApiUrl, links.getDocumentMetadata());

        return new Links()
                .self(links.getSelf())
                .documentMetadata(metadataLink);
    }
}
