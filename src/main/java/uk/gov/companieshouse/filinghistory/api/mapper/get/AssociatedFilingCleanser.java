package uk.gov.companieshouse.filinghistory.api.mapper.get;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAssociatedFilings;

@Component
public class AssociatedFilingCleanser {

    List<FilingHistoryItemDataAssociatedFilings> removeOriginalDescription(
            List<FilingHistoryItemDataAssociatedFilings> associatedFilings) {
        return null;
    }

    List<FilingHistoryItemDataAssociatedFilings> removeDuplicateModelArticles(
            List<FilingHistoryItemDataAssociatedFilings> associatedFilings) {
        return null;
    }


}
