package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAssociatedFilings;

class AssociatedFilingCleanserTest {

    private static final String MODEL_ARTICLES_ADOPTED = "model-articles-adopted";
    private static final String STATEMENT_OF_CAPITAL = "statement-of-capital";

    private final AssociatedFilingCleanser associatedFilingCleanser = new AssociatedFilingCleanser();

    @Test
    void shouldRemoveDuplicateModelArticlesAndSortAssociatedFilingsDescendingAlphabeticalOrder() {
        // given
        var filingNotDuplicate = new FilingHistoryItemDataAssociatedFilings()
                .description(STATEMENT_OF_CAPITAL);
        var modelArticle = new FilingHistoryItemDataAssociatedFilings()
                .description(MODEL_ARTICLES_ADOPTED)
                .date("1998-12-12");

        List<FilingHistoryItemDataAssociatedFilings> duplicateFilings = List.of(
                modelArticle,
                new FilingHistoryItemDataAssociatedFilings()
                        .description(MODEL_ARTICLES_ADOPTED)
                        .date("2001-03-20"),
                new FilingHistoryItemDataAssociatedFilings().description(MODEL_ARTICLES_ADOPTED),
                new FilingHistoryItemDataAssociatedFilings().description(MODEL_ARTICLES_ADOPTED),
                filingNotDuplicate);

        List<FilingHistoryItemDataAssociatedFilings> expected = List.of(filingNotDuplicate, modelArticle);

        // when
        List<FilingHistoryItemDataAssociatedFilings> actual = associatedFilingCleanser.removeDuplicateModelArticles(
                duplicateFilings);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldNotRemoveDuplicateFilingsWithDescriptionsOtherThanModelArticlesAdopted() {
        // given
        List<FilingHistoryItemDataAssociatedFilings> unsortedFilings = Arrays.asList(
                new FilingHistoryItemDataAssociatedFilings().description(MODEL_ARTICLES_ADOPTED),
                new FilingHistoryItemDataAssociatedFilings().description(STATEMENT_OF_CAPITAL),
                new FilingHistoryItemDataAssociatedFilings().description(STATEMENT_OF_CAPITAL),
                new FilingHistoryItemDataAssociatedFilings().description(STATEMENT_OF_CAPITAL));

        // when
        List<FilingHistoryItemDataAssociatedFilings> actual = associatedFilingCleanser.removeDuplicateModelArticles(
                unsortedFilings);

        // then
        assertEquals(unsortedFilings, actual);
    }

    @Test
    void shouldNotDoAnythingWhenSingletonAssociatedFilingsList() {

    }

}
