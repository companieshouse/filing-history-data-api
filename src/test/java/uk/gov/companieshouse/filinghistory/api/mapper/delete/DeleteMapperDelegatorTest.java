package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.filinghistory.api.exception.InternalServerErrorException;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

@ExtendWith(MockitoExtension.class)
class DeleteMapperDelegatorTest {

    private static final String ENTITY_ID = "entity ID";
    private static final String COMPOSITE_RES_TYPE = "RESOLUTIONS";
    private static final String CHILD_ENTITY_ID = "child entity ID";
    private static final String PARENT_TYPE = "CERTNM";

    @InjectMocks
    private DeleteMapperDelegator mapperFactory;
    @Mock
    private CompositeResolutionDeleteMapper compositeResolutionDeleteMapper;

    @Test
    void shouldCallCompositeResolutionMapperWhenCompositeResTypeAndResEntityIdMatches() {
        // given
        FilingHistoryDocument document = new FilingHistoryDocument()
                .entityId(ENTITY_ID)
                .data(new FilingHistoryData()
                        .type(COMPOSITE_RES_TYPE)
                        .resolutions(List.of(
                                new FilingHistoryResolution()
                                        .entityId("first ID"),
                                new FilingHistoryResolution()
                                        .entityId(ENTITY_ID))));

        when(compositeResolutionDeleteMapper.removeTransaction(anyInt(), any())).thenReturn(
                Optional.of(new FilingHistoryDocument()));

        // when
        Optional<FilingHistoryDocument> actual = mapperFactory.delegateDelete(ENTITY_ID, document);

        // then
        assertTrue(actual.isPresent());
        verify(compositeResolutionDeleteMapper).removeTransaction(1, document);
    }

    @Test
    void shouldThrowInternalServerErrorExceptionWhenChildResolutionAndResEntityIdMatches() {
        // given
        FilingHistoryDocument document = new FilingHistoryDocument()
                .entityId(ENTITY_ID)
                .data(new FilingHistoryData()
                        .type(PARENT_TYPE)
                        .resolutions(List.of(new FilingHistoryResolution()
                                .entityId(CHILD_ENTITY_ID))));

        // when
        Executable actual = () -> mapperFactory.delegateDelete(CHILD_ENTITY_ID, document);

        // then
        assertThrows(InternalServerErrorException.class, actual);
    }

    @Test
    void shouldThrowInternalServerErrorExceptionWhenNoEntityIdMatchesAndEmptyResolutionsList() {
        // given
        FilingHistoryDocument document = new FilingHistoryDocument()
                .entityId(ENTITY_ID)
                .data(new FilingHistoryData()
                        .resolutions(List.of()));

        // when
        Executable actual = () -> mapperFactory.delegateDelete(CHILD_ENTITY_ID, document);

        // then
        assertThrows(InternalServerErrorException.class, actual);
    }

    @Test
    void shouldThrowInternalServerErrorExceptionWhenNoEntityIdMatchesAndHasResolution() {
        // given
        FilingHistoryDocument document = new FilingHistoryDocument()
                .entityId(ENTITY_ID)
                .data(new FilingHistoryData()
                        .resolutions(List.of(new FilingHistoryResolution())));

        // when
        Executable actual = () -> mapperFactory.delegateDelete(CHILD_ENTITY_ID, document);

        // then
        assertThrows(InternalServerErrorException.class, actual);
    }

    @Test
    void shouldReturnTopLevelMapperWhenTopLevelEntityIdMatches() {
        // given
        FilingHistoryDocument document = new FilingHistoryDocument()
                .entityId(ENTITY_ID)
                .data(new FilingHistoryData());

        // when
        Optional<FilingHistoryDocument> actual = mapperFactory.delegateDelete(ENTITY_ID, document);

        // then
        assertTrue(actual.isEmpty());
    }
}