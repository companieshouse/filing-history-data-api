package uk.gov.companieshouse.filinghistory.api.mapper.get;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;

@Component
public class FilingHistoryItemCleanser {

    private final AssociatedFilingCleanser associatedFilingCleanser;
    private final DescriptionValuesCleanser descriptionValuesCleanser;

    public FilingHistoryItemCleanser(AssociatedFilingCleanser associatedFilingCleanser,
            DescriptionValuesCleanser descriptionValuesCleanser) {
        this.associatedFilingCleanser = associatedFilingCleanser;
        this.descriptionValuesCleanser = descriptionValuesCleanser;
    }

    ExternalData cleanseFilingHistoryItem(ExternalData externalData) {
        Optional.ofNullable(externalData.getAssociatedFilings())
                .map(filings -> associatedFilingCleanser.removeDuplicateModelArticles(externalData.getType(), filings))
                .ifPresent(externalData::associatedFilings);

        Optional.ofNullable(externalData.getDescriptionValues())
                .map(values ->
                        descriptionValuesCleanser.replaceBackslashesWithWhitespace(externalData.getCategory(), values))
                .ifPresent(externalData::descriptionValues);

        return externalData;
    }
}
