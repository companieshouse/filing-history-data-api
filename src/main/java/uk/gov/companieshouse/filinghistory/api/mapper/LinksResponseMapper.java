package uk.gov.companieshouse.filinghistory.api.mapper;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryLinks;

@Component
public class LinksResponseMapper {

    public FilingHistoryItemDataLinks map(FilingHistoryLinks links) {
        return new FilingHistoryItemDataLinks()
                .self(links.getSelf())
                .documentMetadata(links.getDocumentMetadata());
    }
}
