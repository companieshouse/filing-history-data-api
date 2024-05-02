package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

@ExtendWith(MockitoExtension.class)
class ResolutionTransactionMapperTest {

    private static final String ENTITY_ID = "1234567890";
    private static final String PARENT_ENTITY_ID = "0987654321";
    private static final String COMPANY_NUMBER = "123456789";
    private static final String NEWEST_REQUEST_DELTA_AT = "20151025185208001000";
    private static final Instant INSTANT = Instant.now();
    private static final String UPDATED_BY = "84746291";

    @InjectMocks
    private ResolutionTransactionMapper resolutionTransactionMapper;
    @Mock
    private DataMapper dataMapper;
    @Mock
    private ChildListMapper<FilingHistoryResolution> childListMapper;

    @Mock
    private List<FilingHistoryResolution> resolutionList;

    @Test
    void shouldMapResolutionsListAndAllTopLevelFieldsWhenTopLevelOrCompositeResolution() {
        // given
        ExternalData externalData = new ExternalData()
                .date("2011-11-26T11:27:55.000Z")
                .barcode("barcode")
                .paperFiled(true);
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .companyNumber(COMPANY_NUMBER)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT)
                        .updatedBy(UPDATED_BY)
                        .originalDescription("original description"))
                .externalData(externalData);

        FilingHistoryData existingData = new FilingHistoryData()
                .resolutions(resolutionList);
        FilingHistoryDocument existingDocument = new FilingHistoryDocument()
                .data(existingData);

        FilingHistoryData expectedData = new FilingHistoryData()
                .paperFiled(true)
                .date(Instant.parse("2011-11-26T11:27:55.000Z"))
                .resolutions(resolutionList);
        FilingHistoryDocument expected = new FilingHistoryDocument()
                .data(expectedData)
                .entityId(ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .deltaAt(NEWEST_REQUEST_DELTA_AT)
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(INSTANT)
                        .by(UPDATED_BY))
                .barcode("barcode")
                .originalDescription("original description");

        when(dataMapper.map(any(), any())).thenReturn(expectedData);

        // when
        FilingHistoryDocument actual =
                resolutionTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, existingDocument,
                        INSTANT);

        // then
        assertEquals(expected, actual);
        verify(dataMapper).map(externalData, existingData);
        verify(childListMapper).mapChildList(eq(request), eq(resolutionList), any());
    }

    @Test
    void shouldMapResolutionsListAndSomeTopLevelFieldsWhenChildResolution() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .parentEntityId(PARENT_ENTITY_ID)
                        .companyNumber(COMPANY_NUMBER)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT)
                        .updatedBy(UPDATED_BY)
                        .originalDescription("original description"))
                .externalData(new ExternalData()
                        .date("2011-11-26T11:27:55.000Z")
                        .barcode("barcode")
                        .paperFiled(true));

        FilingHistoryDocument existingDocument = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .resolutions(resolutionList));

        FilingHistoryDocument expected = new FilingHistoryDocument()
                .data(new FilingHistoryData()
                        .paperFiled(true)
                        .resolutions(resolutionList))
                .entityId(PARENT_ENTITY_ID)
                .companyNumber(COMPANY_NUMBER)
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(INSTANT)
                        .by(UPDATED_BY));

        // when
        FilingHistoryDocument actual =
                resolutionTransactionMapper.mapFilingHistoryToExistingDocumentUnlessStale(request, existingDocument,
                        INSTANT);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(dataMapper);
        verify(childListMapper).mapChildList(eq(request), eq(resolutionList), any());
    }
}
