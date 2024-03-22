package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryLinks;

class LinksMapperTest {

    private static final String SELF_LINK = "/company/12345678/filing-history/abc123def456ghi789";
    private final LinksMapper mapper = new LinksMapper();

    @Test
    void shouldMapRequestLinksToPersistenceModelLinks() {
        // given
        FilingHistoryItemDataLinks requestLinks = new FilingHistoryItemDataLinks()
                .self(SELF_LINK);

        FilingHistoryLinks expected = new FilingHistoryLinks()
                .self(SELF_LINK);
        // when
        FilingHistoryLinks actual = mapper.map(requestLinks);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldMapNullLinksToNull() {
        // given

        // when
        FilingHistoryLinks actual = mapper.map(null);

        // then
        assertNull(actual);
    }
}