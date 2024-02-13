package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryLinks;

class LinksGetResponseMapperTest {

    private static final String SELF_LINK = "/company/12345678/filing-history/Mkv123";
    private static final String DOCUMENT_METADATA = "/document/C1_z-KlM567zSgwJz8uN-UZ3_xnGfCljj3k7L69LxwA";

    private final LinksGetResponseMapper mapper = new LinksGetResponseMapper();

    @Test
    void shouldSuccessfullyMapLinks() {
        // given
        final FilingHistoryItemDataLinks expected = new FilingHistoryItemDataLinks()
                .self(SELF_LINK)
                .documentMetadata(DOCUMENT_METADATA);

        // when
        final FilingHistoryItemDataLinks actual = mapper.map(buildDocumentFilingHistoryLinks());

        // then
        assertEquals(expected, actual);
    }

    private static FilingHistoryLinks buildDocumentFilingHistoryLinks() {
        return new FilingHistoryLinks()
                .self(SELF_LINK)
                .documentMetadata(DOCUMENT_METADATA);
    }
}
