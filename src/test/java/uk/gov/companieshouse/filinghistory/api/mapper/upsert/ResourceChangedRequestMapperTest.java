package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.filinghistory.api.mapper.get.ItemGetResponseMapper;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.ResourceChangedRequest;

@ExtendWith(MockitoExtension.class)
class ResourceChangedRequestMapperTest {

    private static final Instant UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    private static final String FILING_HISTORY = "filing-history";
    private static final String EXPECTED_CONTEXT_ID = "35234234";
    private static final ExternalData deletedData = new ExternalData();
    private static final FilingHistoryDocument filingHistoryDocument = new FilingHistoryDocument()
            .companyNumber("12345678")
            .transactionId("ABCDE54321");
    private static final String RESOURCE_URI = "/company/12345678/filing-history/ABCDE54321";
    private static final String CHANGED_EVENT_TYPE = "changed";
    private static final String DELETED_EVENT_TYPE = "deleted";

    @InjectMocks
    private ResourceChangedRequestMapper mapper;

    @Mock
    private ItemGetResponseMapper itemGetResponseMapper;
    @Mock
    private Supplier<Instant> instantSupplier;

    @Test
    void testUpsertResourceChangedMapper() {
        // given
        when(instantSupplier.get()).thenReturn(UPDATED_AT);

        ResourceChangedRequest upsertResourceChangedRequest = new ResourceChangedRequest(EXPECTED_CONTEXT_ID,
                filingHistoryDocument, false);
        ChangedResource expectedChangedResource = new ChangedResource()
                .contextId(EXPECTED_CONTEXT_ID)
                .resourceUri(RESOURCE_URI)
                .resourceKind(FILING_HISTORY)
                .event(new ChangedResourceEvent()
                        .type(CHANGED_EVENT_TYPE)
                        .publishedAt(UPDATED_AT.toString()));

        // when
        ChangedResource actual = mapper.mapChangedResource(upsertResourceChangedRequest);

        // then
        assertEquals(expectedChangedResource, actual);
    }

    @Test
    void testDeletedResourceChangedMapper() {
        // given
        when(instantSupplier.get()).thenReturn(UPDATED_AT);
        when(itemGetResponseMapper.mapFilingHistoryItem(any())).thenReturn(deletedData);

        ResourceChangedRequest deleteResourceChangedRequest = new ResourceChangedRequest(EXPECTED_CONTEXT_ID,
                filingHistoryDocument, true);
        ChangedResource expectedChangedResource = new ChangedResource()
                .contextId(EXPECTED_CONTEXT_ID)
                .resourceUri(RESOURCE_URI)
                .resourceKind(FILING_HISTORY)
                .deletedData(deletedData)
                .event(new ChangedResourceEvent()
                        .type(DELETED_EVENT_TYPE)
                        .publishedAt(UPDATED_AT.toString()));

        // when
        ChangedResource actual = mapper.mapChangedResource(deleteResourceChangedRequest);

        // then
        assertEquals(expectedChangedResource, actual);
        verify(itemGetResponseMapper).mapFilingHistoryItem(filingHistoryDocument);
    }
}
