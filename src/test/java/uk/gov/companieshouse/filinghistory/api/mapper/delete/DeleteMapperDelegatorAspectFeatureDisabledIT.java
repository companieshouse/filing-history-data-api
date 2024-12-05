package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.filinghistory.api.exception.BadRequestException;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeleteAggregate;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@SpringBootTest
class DeleteMapperDelegatorAspectFeatureDisabledIT {

    private static final String ENTITY_ID = "entity ID";
    private static final String DELTA_AT = "20151025185208001000";
    private static final String CHILD_ENTITY_ID = "child entity ID";

    @Autowired
    private DeleteMapperDelegator deleteMapperDelegator;
    @MockitoBean
    private CompositeResolutionDeleteMapper compositeResolutionDeleteMapper;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("feature.delete_child_transactions.disabled", () -> true);
    }

    @Test
    void shouldDeleteTopLevelTransactionsWhenFeatureDisabled() {

        // when
        Optional<FilingHistoryDocument> actual = deleteMapperDelegator.delegateDelete(ENTITY_ID,
                new FilingHistoryDeleteAggregate()
                        .document(new FilingHistoryDocument()
                                .entityId(ENTITY_ID)
                                .data(new FilingHistoryData())), DELTA_AT);

        // then
        assertTrue(actual.isEmpty());
        verifyNoInteractions(compositeResolutionDeleteMapper);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenResolutionChildMatch() {
        // given

        // when
        Executable actual = () -> deleteMapperDelegator.delegateDelete(CHILD_ENTITY_ID,
                new FilingHistoryDeleteAggregate()
                        .document(new FilingHistoryDocument()
                                .entityId(ENTITY_ID))
                        .resolutionIndex(0), DELTA_AT);

        // then
        assertThrows(BadRequestException.class, actual);
        verifyNoInteractions(compositeResolutionDeleteMapper);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenAnnotationChildMatch() {
        // given

        // when
        Executable actual = () -> deleteMapperDelegator.delegateDelete(CHILD_ENTITY_ID,
                new FilingHistoryDeleteAggregate()
                        .document(new FilingHistoryDocument()
                                .entityId(ENTITY_ID))
                        .annotationIndex(0), DELTA_AT);

        // then
        assertThrows(BadRequestException.class, actual);
        verifyNoInteractions(compositeResolutionDeleteMapper);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenAssociatedFilingChildMatch() {
        // given

        // when
        Executable actual = () -> deleteMapperDelegator.delegateDelete(CHILD_ENTITY_ID,
                new FilingHistoryDeleteAggregate()
                        .document(new FilingHistoryDocument()
                                .entityId(ENTITY_ID))
                        .associatedFilingIndex(0), DELTA_AT);

        // then
        assertThrows(BadRequestException.class, actual);
        verifyNoInteractions(compositeResolutionDeleteMapper);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenCompositeResolutionMatch() {
        // given

        // when
        Executable actual = () -> deleteMapperDelegator.delegateDelete(ENTITY_ID,
                new FilingHistoryDeleteAggregate()
                        .document(new FilingHistoryDocument()
                                .entityId(ENTITY_ID)
                                .data(new FilingHistoryData()
                                        .type("RESOLUTIONS"))), DELTA_AT);

        // then
        assertThrows(BadRequestException.class, actual);
        verifyNoInteractions(compositeResolutionDeleteMapper);
    }
}
