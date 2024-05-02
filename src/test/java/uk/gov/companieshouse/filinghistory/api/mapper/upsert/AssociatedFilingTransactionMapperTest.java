package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAssociatedFiling;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@ExtendWith(MockitoExtension.class)
class AssociatedFilingTransactionMapperTest {

    private static final String PARENT_ENTITY_ID = "0987654321";
    private static final String COMPANY_NUMBER = "123456789";
    private static final Instant INSTANT = Instant.now();
    private static final String UPDATED_BY = "84746291";

    @InjectMocks
    private AssociatedFilingTransactionMapper associatedFilingTransactionMapper;

    @Mock
    private AssociatedFilingChildMapper associatedFilingChildMapper;
    @Mock
    private ChildListMapper<FilingHistoryAssociatedFiling> childListMapper;

    @Mock
    private List<FilingHistoryAssociatedFiling> associatedFilingList;
    @Mock
    private FilingHistoryAssociatedFiling associatedFiling;
    @Mock
    private InternalFilingHistoryApi mockRequest;

    @Test
    void shouldMapFilingHistoryRequestToMongoDocument() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .parentEntityId(PARENT_ENTITY_ID)
                        .companyNumber(COMPANY_NUMBER)
                        .updatedBy(UPDATED_BY))
                .externalData(new ExternalData()
                        .paperFiled(true));

        FilingHistoryDocument existingDocument = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .associatedFilings(associatedFilingList));

        FilingHistoryDocument expected = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .paperFiled(true)
                        .associatedFilings(associatedFilingList))
                .entityId(PARENT_ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(INSTANT)
                        .by(UPDATED_BY));

        // when
        FilingHistoryDocument actual =
                associatedFilingTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, existingDocument, INSTANT);

        // then
        assertEquals(expected, actual);
        verify(childListMapper).mapChildList(eq(request), eq(associatedFilingList), any());
    }

    @Test
    void shouldMapFilingHistoryData() {
        // given
        final FilingHistoryData expected = new FilingHistoryData()
                .associatedFilings(List.of(associatedFiling));

        when(associatedFilingChildMapper.mapChild(any())).thenReturn(associatedFiling);

        // when
        final FilingHistoryData actual = associatedFilingTransactionMapper.mapFilingHistoryData(mockRequest, new FilingHistoryData());

        // then
        assertEquals(expected, actual);
        verify(associatedFilingChildMapper).mapChild(mockRequest);
    }
}
