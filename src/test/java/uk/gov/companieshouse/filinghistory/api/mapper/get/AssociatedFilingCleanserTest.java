package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.AssociatedFiling;

class AssociatedFilingCleanserTest {

    private static final String MODEL_ARTICLES_ADOPTED = "model-articles-adopted";
    private static final String STATEMENT_OF_CAPITAL = "statement-of-capital";
    private static final String NEW_INC = "NEWINC";

    private final AssociatedFilingCleanser associatedFilingCleanser = new AssociatedFilingCleanser();

    @Test
    void shouldRemoveDuplicateModelArticlesAndSortAssociatedFilingsDescendingAlphabeticalOrder() {
        // given
        var filingNotDuplicate = new AssociatedFiling()
                .description(STATEMENT_OF_CAPITAL);
        var modelArticle = new AssociatedFiling()
                .description(MODEL_ARTICLES_ADOPTED)
                .date("1998-12-12");

        List<AssociatedFiling> duplicateFilings = List.of(
                modelArticle,
                new AssociatedFiling()
                        .description(MODEL_ARTICLES_ADOPTED)
                        .date("2001-03-20"),
                new AssociatedFiling().description(MODEL_ARTICLES_ADOPTED),
                new AssociatedFiling().description(MODEL_ARTICLES_ADOPTED),
                filingNotDuplicate);

        List<AssociatedFiling> expected = List.of(filingNotDuplicate, modelArticle);

        // when
        List<AssociatedFiling> actual = associatedFilingCleanser.removeDuplicateModelArticles(NEW_INC, duplicateFilings);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldNotRemoveDuplicateFilingsWithDescriptionsOtherThanModelArticlesAdopted() {
        // given
        List<AssociatedFiling> unsortedFilings = Arrays.asList(
                new AssociatedFiling().description(MODEL_ARTICLES_ADOPTED),
                new AssociatedFiling().description(STATEMENT_OF_CAPITAL),
                new AssociatedFiling().description(STATEMENT_OF_CAPITAL),
                new AssociatedFiling().description(STATEMENT_OF_CAPITAL));

        // when
        List<AssociatedFiling> actual = associatedFilingCleanser.removeDuplicateModelArticles(NEW_INC, unsortedFilings);

        // then
        assertEquals(unsortedFilings, actual);
    }

    @Test
    void shouldNotDoAnythingWhenSingletonAssociatedFilingsList() {
        // given
        List<AssociatedFiling> filings = List.of(new AssociatedFiling());

        // when
        List<AssociatedFiling> actual = associatedFilingCleanser.removeDuplicateModelArticles(NEW_INC, filings);

        // then
        assertEquals(filings, actual);
    }

    @Test
    void shouldNotRemoveDuplicateAssociatedFilingsIfTypeNotNewInc() {
        // given
        List<AssociatedFiling> filings = List.of(new AssociatedFiling(), new AssociatedFiling());

        // when
        List<AssociatedFiling> actual = associatedFilingCleanser.removeDuplicateModelArticles("", filings);

        // then
        assertEquals(filings, actual);
    }
}
