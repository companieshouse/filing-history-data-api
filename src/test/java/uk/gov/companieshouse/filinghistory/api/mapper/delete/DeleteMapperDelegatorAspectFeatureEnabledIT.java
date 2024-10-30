package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeleteAggregate;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

@SpringBootTest
class DeleteMapperDelegatorAspectFeatureEnabledIT {

    private static final String ENTITY_ID = "entity ID";
    private static final String DELTA_AT = "20151025185208001000";
    private static final String COMPOSITE_RES_TYPE = "RESOLUTIONS";

    @Autowired
    private DeleteMapperDelegator deleteMapperDelegator;
    @MockBean
    private CompositeResolutionDeleteMapper compositeResolutionDeleteMapper;

    @Test
    void shouldDeleteChildTransactionsWhenFeatureEnabled() {
        FilingHistoryDeleteAggregate aggregate = new FilingHistoryDeleteAggregate()
                .resolutionIndex(1)
                .document(new FilingHistoryDocument()
                        .entityId(ENTITY_ID)
                        .data(new FilingHistoryData()
                                .type(COMPOSITE_RES_TYPE)
                                .resolutions(List.of(
                                        new FilingHistoryResolution()
                                                .entityId("first ID"),
                                        new FilingHistoryResolution()
                                                .entityId(ENTITY_ID)))));

        when(compositeResolutionDeleteMapper.removeTransaction(anyInt(), any(), any())).thenReturn(
                Optional.of(new FilingHistoryDocument()));

        // when
        Optional<FilingHistoryDocument> actual = deleteMapperDelegator.delegateDelete(ENTITY_ID, aggregate, DELTA_AT);

        // then
        assertTrue(actual.isPresent());
        verify(compositeResolutionDeleteMapper).removeTransaction(1, DELTA_AT, aggregate.getDocument());
    }
}
