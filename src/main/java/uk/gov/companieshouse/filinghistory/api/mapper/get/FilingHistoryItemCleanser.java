package uk.gov.companieshouse.filinghistory.api.mapper.get;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAssociatedFilings;

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
        //so get the associated filings and remove any duplicate model items that come back if the type is NEWINC
        Optional<List<FilingHistoryItemDataAssociatedFilings>> associatedFilings = Optional.ofNullable(externalData.getAssociatedFilings());
                if("NEWINC".equals(externalData.getType())) {
                    associatedFilings.map(associatedFilingCleanser::removeDuplicateModelArticles);
                }
                associatedFilings.map(associatedFilingCleanser::removeOriginalDescription)
                        .ifPresent(externalData::associatedFilings);

        if ("ANNOTATION".equals(externalData.getType())) {
            externalData.annotations(null);
        }

        Optional.ofNullable(externalData.getDescriptionValues())
                .map(values -> descriptionValuesCleanser.replaceBackslashesWithWhiteSpace(
                        externalData.getCategory(), values))
                .ifPresent(externalData::descriptionValues);

        return externalData;
    }
}
