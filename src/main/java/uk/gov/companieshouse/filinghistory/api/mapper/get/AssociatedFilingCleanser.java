package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static java.util.Comparator.comparing;
import static uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication.NAMESPACE;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAssociatedFilings;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class AssociatedFilingCleanser {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String MODEL_ARTICLES_ADOPTED = "model-articles-adopted";

    List<FilingHistoryItemDataAssociatedFilings> removeOriginalDescription(
            List<FilingHistoryItemDataAssociatedFilings> associatedFilings) {
        return null;
    }

    List<FilingHistoryItemDataAssociatedFilings> removeDuplicateModelArticles(
            List<FilingHistoryItemDataAssociatedFilings> associatedFilings) {
        if (associatedFilings.size() > 1) {
            List<FilingHistoryItemDataAssociatedFilings> modelArticles = associatedFilings.stream()
                    .filter(filing -> MODEL_ARTICLES_ADOPTED.equals(filing.getDescription()))
                    .toList();

            if (modelArticles.size() > 1) {
                LOGGER.info("Duplicate model-articles-adopted found", DataMapHolder.getLogMap());
                List<FilingHistoryItemDataAssociatedFilings> filings = associatedFilings.stream()
                        .filter(filing -> !MODEL_ARTICLES_ADOPTED.equals(filing.getDescription()))
                        .collect(Collectors.toCollection(ArrayList::new));
                filings.add(modelArticles.getFirst());

                return filings.stream()
                        .sorted(comparing(FilingHistoryItemDataAssociatedFilings::getDescription).reversed())
                        .toList();
            }
        }
        return associatedFilings;
    }
}
