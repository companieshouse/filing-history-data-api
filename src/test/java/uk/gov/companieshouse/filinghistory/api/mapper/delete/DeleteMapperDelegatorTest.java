package uk.gov.companieshouse.filinghistory.api.mapper.delete;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
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
import uk.gov.companieshouse.filinghistory.api.exception.BadRequestException;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeleteAggregate;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;
import uk.gov.companieshouse.filinghistory.api.serdes.FilingHistoryDocumentCopier;

@ExtendWith(MockitoExtension.class)
class DeleteMapperDelegatorTest {

    private static final String ENTITY_ID = "entity ID";
    private static final String COMPOSITE_RES_TYPE = "RESOLUTIONS";
    private static final String CHILD_ENTITY_ID = "child entity ID";
    private static final String PARENT_TYPE = "CERTNM";

    @InjectMocks
    private DeleteMapperDelegator deleteMapperDelegator;
    @Mock
    private FilingHistoryDocumentCopier documentCopier;
    @Mock
    private CompositeResolutionDeleteMapper compositeResolutionDeleteMapper;
    @Mock
    private ChildDeleteMapper childDeleteMapper;

    @Mock
    private FilingHistoryDocument document;

    @Test
    void shouldCallCompositeResolutionMapperWhenCompositeResTypeAndResEntityIdMatches() {
        // given
        FilingHistoryDocument documentCopy = new FilingHistoryDocument()
                .entityId(ENTITY_ID)
                .data(new FilingHistoryData()
                        .type(COMPOSITE_RES_TYPE)
                        .resolutions(List.of(
                                new FilingHistoryResolution()
                                        .entityId("first ID"),
                                new FilingHistoryResolution()
                                        .entityId(ENTITY_ID))));
        FilingHistoryDeleteAggregate aggregate = new FilingHistoryDeleteAggregate()
                .resolutionIndex(1)
                .document(document);

        when(documentCopier.deepCopy(any())).thenReturn(documentCopy);
        when(compositeResolutionDeleteMapper.removeTransaction(anyInt(), any())).thenReturn(
                Optional.of(new FilingHistoryDocument()));

        // when
        Optional<FilingHistoryDocument> actual = deleteMapperDelegator.delegateDelete(ENTITY_ID, aggregate);

        // then
        assertTrue(actual.isPresent());
        verify(documentCopier).deepCopy(document);
        verify(compositeResolutionDeleteMapper).removeTransaction(1, documentCopy);
    }

    @Test
    void shouldCallChildDeleteMapperWhenChildResolutionAndResEntityIdMatches() {
        // given
        FilingHistoryDocument documentCopy = new FilingHistoryDocument()
                .entityId(ENTITY_ID)
                .data(new FilingHistoryData()
                        .type(PARENT_TYPE)
                        .resolutions(List.of(
                                new FilingHistoryResolution()
                                        .entityId(CHILD_ENTITY_ID))));
        FilingHistoryDeleteAggregate aggregate = new FilingHistoryDeleteAggregate()
                .resolutionIndex(0)
                .document(document);

        when(documentCopier.deepCopy(any())).thenReturn(documentCopy);
        when(childDeleteMapper.removeTransaction(any(), anyInt(), any(), any(), any())).thenReturn(
                Optional.of(new FilingHistoryDocument()));

        // when
        Optional<FilingHistoryDocument> actual = deleteMapperDelegator.delegateDelete(CHILD_ENTITY_ID, aggregate);

        // then
        assertTrue(actual.isPresent());
        verify(documentCopier).deepCopy(document);
        verify(childDeleteMapper).removeTransaction(eq(CHILD_ENTITY_ID), eq(0), eq(documentCopy), any(), any());
    }

    @Test
    void shouldThrowBadRequestExceptionWhenNoEntityIdMatchesAndEmptyResolutionsList() {
        // given
        FilingHistoryDocument documentCopy = new FilingHistoryDocument()
                .entityId(ENTITY_ID)
                .data(new FilingHistoryData()
                        .resolutions(List.of()));
        FilingHistoryDeleteAggregate aggregate = new FilingHistoryDeleteAggregate()
                .document(document);

        when(documentCopier.deepCopy(any())).thenReturn(documentCopy);

        // when
        Executable actual = () -> deleteMapperDelegator.delegateDelete(CHILD_ENTITY_ID, aggregate);

        // then
        assertThrows(BadRequestException.class, actual);
        verify(documentCopier).deepCopy(document);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenNoEntityIdMatchesAndHasResolution() {
        // given
        FilingHistoryDocument documentCopy = new FilingHistoryDocument()
                .entityId(ENTITY_ID)
                .data(new FilingHistoryData()
                        .resolutions(List.of(new FilingHistoryResolution())));
        FilingHistoryDeleteAggregate aggregate = new FilingHistoryDeleteAggregate()
                .document(document);

        when(documentCopier.deepCopy(any())).thenReturn(documentCopy);

        // when
        Executable actual = () -> deleteMapperDelegator.delegateDelete(CHILD_ENTITY_ID, aggregate);

        // then
        assertThrows(BadRequestException.class, actual);
        verify(documentCopier).deepCopy(document);
    }

    @Test
    void shouldReturnTopLevelMapperWhenTopLevelEntityIdMatches() {
        // given
        FilingHistoryDocument documentCopy = new FilingHistoryDocument()
                .entityId(ENTITY_ID)
                .data(new FilingHistoryData());
        FilingHistoryDeleteAggregate aggregate = new FilingHistoryDeleteAggregate()
                .document(document);

        when(documentCopier.deepCopy(any())).thenReturn(documentCopy);

        // when
        Optional<FilingHistoryDocument> actual = deleteMapperDelegator.delegateDelete(ENTITY_ID, aggregate);

        // then
        assertTrue(actual.isEmpty());
        verify(documentCopier).deepCopy(document);
    }
}