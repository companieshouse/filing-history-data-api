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
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryLinks;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

@ExtendWith(MockitoExtension.class)
class ResolutionTransactionMapperTest {

    private static final String TRANSACTION_ID = "transaction ID";
    private static final String ENTITY_ID = "1234567890";
    private static final String PARENT_ENTITY_ID = "0987654321";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String DELTA_AT = "20151025185208001000";
    private static final Instant UPDATED_AT = Instant.parse("2024-05-02T00:00:00Z");
    private static final String UPDATED_BY = "updated by";
    private static final String DATE = "2011-11-26T11:27:55.000Z";
    private static final Instant CREATED_AT = Instant.parse(DATE);
    private static final String CREATED_BY = "created by";

    @InjectMocks
    private ResolutionTransactionMapper resolutionTransactionMapper;
    @Mock
    private LinksMapper linksMapper;
    @Mock
    private DataMapper dataMapper;
    @Mock
    private ChildListMapper<FilingHistoryResolution> childListMapper;

    @Mock
    private List<FilingHistoryResolution> resolutionList;

    @Test
    void shouldMapResolutionToNewDocumentWhenTopLevelOrComposite() {
        // given
        Links requestLinks = new Links()
                .self("self link");
        ExternalData externalData = new ExternalData()
                .links(requestLinks)
                .date(DATE)
                .barcode("barcode")
                .paperFiled(true);
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .companyNumber(COMPANY_NUMBER)
                        .deltaAt(DELTA_AT)
                        .updatedBy(UPDATED_BY)
                        .originalDescription("original description"))
                .externalData(externalData);

        FilingHistoryLinks expectedLinks = new FilingHistoryLinks()
                .self("self link");
        FilingHistoryData expectedData = new FilingHistoryData()
                .links(expectedLinks)
                .paperFiled(true)
                .date(Instant.parse(DATE));
        FilingHistoryDeltaTimestamp expectedTimestamp = new FilingHistoryDeltaTimestamp()
                .at(UPDATED_AT)
                .by(UPDATED_BY);
        FilingHistoryDocument expected = new FilingHistoryDocument()
                .transactionId(TRANSACTION_ID)
                .data(expectedData)
                .entityId(ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .deltaAt(DELTA_AT)
                .updated(expectedTimestamp)
                .created(expectedTimestamp)
                .barcode("barcode")
                .originalDescription("original description");

        when(linksMapper.map(any())).thenReturn(expectedLinks);
        when(dataMapper.map(any(), any())).thenReturn(expectedData);

        // when
        FilingHistoryDocument actual = resolutionTransactionMapper.mapNewFilingHistory(TRANSACTION_ID, request,
                UPDATED_AT);

        // then
        assertEquals(expected, actual);
        verify(linksMapper).map(requestLinks);
        verify(dataMapper).map(externalData, new FilingHistoryData());
        verify(childListMapper).mapChildList(eq(request), isNull(), any());
    }

    @Test
    void shouldMapResolutionToNewDocumentWhenChildResolution() {
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
        FilingHistoryDocument actual = resolutionTransactionMapper.mapNewFilingHistory(TRANSACTION_ID, request,
                UPDATED_AT);

        // then
        assertEquals(expected, actual);
        verify(linksMapper).map(requestLinks);
        verifyNoInteractions(dataMapper);
        verify(childListMapper).mapChildList(eq(request), isNull(), any());
    }

    @Test
    void shouldMapResolutionToExistingDocumentWhenTopLevelOrComposite() {
        // given
        ExternalData externalData = new ExternalData()
                .date(DATE)
                .barcode("barcode")
                .paperFiled(true);
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .companyNumber(COMPANY_NUMBER)
                        .deltaAt(DELTA_AT)
                        .updatedBy(UPDATED_BY)
                        .originalDescription("original description"))
                .externalData(externalData);

        FilingHistoryDeltaTimestamp existingTimestamp = new FilingHistoryDeltaTimestamp()
                .at(CREATED_AT)
                .by(CREATED_BY);
        FilingHistoryLinks existingLinks = new FilingHistoryLinks()
                .self("self link")
                .documentMetadata("metadata");
        FilingHistoryData existingData = new FilingHistoryData()
                .links(existingLinks)
                .resolutions(resolutionList);
        FilingHistoryDocument existingDocument = new FilingHistoryDocument()
                .data(existingData)
                .created(existingTimestamp)
                .updated(existingTimestamp);

        FilingHistoryData expectedData = new FilingHistoryData()
                .paperFiled(true)
                .date(Instant.parse(DATE))
                .resolutions(resolutionList);
        FilingHistoryDocument expected = new FilingHistoryDocument()
                .data(expectedData)
                .entityId(ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .deltaAt(DELTA_AT)
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(UPDATED_AT)
                        .by(UPDATED_BY))
                .created(existingTimestamp)
                .barcode("barcode")
                .originalDescription("original description");

        when(dataMapper.map(any(), any())).thenReturn(expectedData);

        // when
        FilingHistoryDocument actual = resolutionTransactionMapper.mapExistingFilingHistory(request, existingDocument,
                UPDATED_AT);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(linksMapper);
        verify(dataMapper).map(externalData, existingData);
        verify(childListMapper).mapChildList(eq(request), eq(resolutionList), any());
    }

    @Test
    void shouldMapResolutionToExistingDocumentWhenChildResolution() {
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
        FilingHistoryData existingData = new FilingHistoryData()
                .links(existingLinks)
                .resolutions(resolutionList);
        FilingHistoryDocument existingDocument = new FilingHistoryDocument()
                .data(existingData)
                .created(existingTimestamp)
                .updated(existingTimestamp);

        FilingHistoryDocument expected = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .links(existingLinks)
                        .paperFiled(true)
                        .resolutions(resolutionList))
                .entityId(PARENT_ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(UPDATED_AT)
                        .by(UPDATED_BY))
                .created(existingTimestamp);

        // when
        FilingHistoryDocument actual = resolutionTransactionMapper.mapExistingFilingHistory(request, existingDocument,
                UPDATED_AT);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(linksMapper);
        verifyNoInteractions(dataMapper);
        verify(childListMapper).mapChildList(eq(request), eq(resolutionList), any());
    }
}
