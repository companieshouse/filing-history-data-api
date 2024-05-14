package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

class DeleteMapperDelegatorAspectTest {

    private final DeleteMapperDelegatorAspect aspect = new DeleteMapperDelegatorAspect();

    @Test
    void shouldReturnEmpty() {
        // when
        Optional<FilingHistoryDocument> actual = aspect.deleteChildTransactionsDisabled();

        // then
        assertTrue(actual.isEmpty());
    }
}
