package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import uk.gov.companieshouse.api.filinghistory.Links;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAssociatedFiling;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryLinks;

@ExtendWith(MockitoExtension.class)
class AssociatedFilingTransactionMapperTest {

    private static final String TRANSACTION_ID = "transaction ID";
    private static final String PARENT_ENTITY_ID = "0987654321";
    private static final String COMPANY_NUMBER = "12345678";
    private static final Instant UPDATED_AT = Instant.parse("2024-05-02T00:00:00Z");
    private static final String UPDATED_BY = "updated by";
    private static final Instant CREATED_AT = Instant.parse("2020-05-02T00:00:00Z");
    private static final String CREATED_BY = "created by";


    @InjectMocks
    private AssociatedFilingTransactionMapper associatedFilingTransactionMapper;
    @Mock
    private LinksMapper linksMapper;
    @Mock
    private ChildListMapper<FilingHistoryAssociatedFiling> childListMapper;

    @Mock
    private List<FilingHistoryAssociatedFiling> associatedFilingList;

    @Test
    void shouldMapAssociatedFilingToNewDocument() {
        // given
        Links requestLinks = new Links()
                .self("self link");
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .parentEntityId(PARENT_ENTITY_ID)
                        .companyNumber(COMPANY_NUMBER)
                        .updatedBy(UPDATED_BY))
                .externalData(new ExternalData()
                        .links(requestLinks)
                        .paperFiled(true));

        FilingHistoryLinks expectedLinks = new FilingHistoryLinks()
                .self("self link");
        FilingHistoryDeltaTimestamp expectedTimestamp = new FilingHistoryDeltaTimestamp()
                .at(UPDATED_AT)
                .by(UPDATED_BY);
        FilingHistoryDocument expected = new FilingHistoryDocument()
                .transactionId(TRANSACTION_ID)
                .data(new FilingHistoryData()
                        .links(expectedLinks)
                        .paperFiled(true))
                .entityId(PARENT_ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .updated(expectedTimestamp)
                .created(expectedTimestamp);

        when(linksMapper.map(any())).thenReturn(expectedLinks);

        // when
        FilingHistoryDocument actual = associatedFilingTransactionMapper.mapNewFilingHistory(TRANSACTION_ID, request,
                UPDATED_AT);

        // then
        assertEquals(expected, actual);
        verify(linksMapper).map(requestLinks);
        verify(childListMapper).mapChildList(eq(request), isNull(), any());
    }

    @Test
    void shouldMapAssociatedFilingToExistingDocument() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .parentEntityId(PARENT_ENTITY_ID)
                        .companyNumber(COMPANY_NUMBER)
                        .updatedBy(UPDATED_BY))
                .externalData(new ExternalData()
                        .paperFiled(true));

        FilingHistoryDeltaTimestamp existingTimestamp = new FilingHistoryDeltaTimestamp()
                .at(CREATED_AT)
                .by(CREATED_BY);
        FilingHistoryLinks existingLinks = new FilingHistoryLinks()
                .self("self link")
                .documentMetadata("metadata");
        FilingHistoryDocument existingDocument = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .links(existingLinks)
                        .associatedFilings(associatedFilingList))
                .created(existingTimestamp)
                .updated(existingTimestamp);

        FilingHistoryDocument expected = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .links(existingLinks)
                        .paperFiled(true)
                        .associatedFilings(associatedFilingList))
                .entityId(PARENT_ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(UPDATED_AT)
                        .by(UPDATED_BY))
                .created(existingTimestamp);

        // when
        FilingHistoryDocument actual =
                associatedFilingTransactionMapper.mapExistingFilingHistory(request, existingDocument, UPDATED_AT);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(linksMapper);
        verify(childListMapper).mapChildList(eq(request), eq(associatedFilingList), any());
    }
}
