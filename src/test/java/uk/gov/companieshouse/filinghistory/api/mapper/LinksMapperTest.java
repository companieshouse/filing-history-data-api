package uk.gov.companieshouse.filinghistory.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryLinks;

class LinksMapperTest {

    public static final String SELF_LINK = "/company/12345678/filing-history/abc123def456ghi789";
    public static final String METADATA = "metadata"; // this is unlikely to come across in a request
    private final LinksMapper mapper = new LinksMapper();

    @Test
    void shouldMapRequestLinksToPersistenceModelLinks() {
        // given
        FilingHistoryItemDataLinks requestLinks = new FilingHistoryItemDataLinks()
                .self(SELF_LINK)
                .documentMetadata(METADATA);

        FilingHistoryLinks expected = new FilingHistoryLinks()
                .self(SELF_LINK)
                .documentMetadata(METADATA);
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