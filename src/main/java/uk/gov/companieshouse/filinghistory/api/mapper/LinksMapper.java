package uk.gov.companieshouse.filinghistory.api.mapper;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryLinks;

@Component
public class LinksMapper {

    FilingHistoryLinks map(final FilingHistoryItemDataLinks requestLinks) {
        return Optional.ofNullable(requestLinks)
                .map(links -> new FilingHistoryLinks()
                        .self(links.getSelf()))
                .orElse(null);
    }
}
