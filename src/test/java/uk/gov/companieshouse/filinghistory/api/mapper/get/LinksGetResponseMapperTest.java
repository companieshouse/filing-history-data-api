package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.filinghistory.Links;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryLinks;

class LinksGetResponseMapperTest {

    private static final String SELF_LINK = "/company/12345678/filing-history/Mkv123";
    private static final String GET_RESPONSE_METADATA = "http://localhost:8080/document/C1_z-KlM567zSgwJz8uN-UZ3_xnGfCljj3k7L69LxwA";
    private static final String DOCUMENT_METADATA = "/document/C1_z-KlM567zSgwJz8uN-UZ3_xnGfCljj3k7L69LxwA";
    private static final String DOCUMENT_API_URL = "http://localhost:8080";

    private final LinksGetResponseMapper mapper = new LinksGetResponseMapper(DOCUMENT_API_URL);

    @Test
    void shouldSuccessfullyMapLinks() {
        // given
        final Links expected = new Links()
                .self(SELF_LINK)
                .documentMetadata(GET_RESPONSE_METADATA);

        // when
        final Links actual = mapper.map(buildDocumentFilingHistoryLinks());

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldSuccessfullyMapLinksWithNullDocumentMetadata() {
        // given
        final Links expected = new Links()
                .self(SELF_LINK);

        // when
        final Links actual = mapper.map(buildDocumentFilingHistoryLinksWithNullMetadata());

        // then
        assertEquals(expected, actual);
    }

    private static FilingHistoryLinks buildDocumentFilingHistoryLinks() {
        return new FilingHistoryLinks()
                .self(SELF_LINK)
                .documentMetadata(DOCUMENT_METADATA);
    }

    private static FilingHistoryLinks buildDocumentFilingHistoryLinksWithNullMetadata() {
        return new FilingHistoryLinks()
                .self(SELF_LINK);
    }
}
