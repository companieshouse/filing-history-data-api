package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.AssociatedFiling;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAssociatedFiling;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDescriptionValues;

@ExtendWith(MockitoExtension.class)
class AssociatedFilingChildMapperTest {

    private static final String ENTITY_ID = "1234567890";
    private static final String NEWEST_REQUEST_DELTA_AT = "20151025185208001000";
    private static final String ACTION_DATE = "2015-10-05T00:00:00Z";
    private static final Instant INSTANT_ACTION_DATE = Instant.parse("2015-10-05T00:00:00Z");

    @InjectMocks
    private AssociatedFilingChildMapper associatedFilingChildMapper;

    @Mock
    private DescriptionValuesMapper descriptionValuesMapper;

    @Mock
    private DescriptionValues requestDescriptionValues;
    @Mock
    private FilingHistoryDescriptionValues descriptionValues;

    @Test
    void shouldAddNewAssociatedFilingWhenNewObjectPassedInArgs() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT))
                .externalData(new ExternalData()
                        .associatedFilings(List.of(
                                new AssociatedFiling()
                                        .actionDate(ACTION_DATE)
                                        .category("annual-return")
                                        .originalDescription("original description")
                                        .description("legacy")
                                        .descriptionValues(requestDescriptionValues)
                                        .type("363(288)")
                                        .date("2005-05-10T12:00:00.000Z")
                        )));

        FilingHistoryAssociatedFiling expected = new FilingHistoryAssociatedFiling()
                .actionDate(INSTANT_ACTION_DATE)
                .entityId(ENTITY_ID)
                .deltaAt(NEWEST_REQUEST_DELTA_AT)
                .category("annual-return")
                .date(Instant.parse("2005-05-10T12:00:00.000Z"))
                .description("legacy")
                .descriptionValues(descriptionValues)
                .type("363(288)");

        when(descriptionValuesMapper.map(any())).thenReturn(descriptionValues);

        // when
        FilingHistoryAssociatedFiling actual = associatedFilingChildMapper.mapChild(request);

        // then
        assertEquals(expected, actual);
        verify(descriptionValuesMapper).map(requestDescriptionValues);
    }

    @Test
    void shouldUpdateExistingAssociatedFiling() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT))
                .externalData(new ExternalData()
                        .associatedFilings(List.of(
                                new AssociatedFiling()
                                        .category("annual-return")
                                        .description("legacy")
                                        .descriptionValues(requestDescriptionValues)
                                        .type("363(288)")
                                        .date("2005-05-10T12:00:00.000Z")
                        )));

        FilingHistoryAssociatedFiling expectedAssociatedFiling = new FilingHistoryAssociatedFiling()
                .entityId(ENTITY_ID)
                .deltaAt(NEWEST_REQUEST_DELTA_AT)
                .category("annual-return")
                .date(Instant.parse("2005-05-10T12:00:00.000Z"))
                .description("legacy")
                .descriptionValues(descriptionValues)
                .type("363(288)");

        FilingHistoryAssociatedFiling existingAssociatedFiling = new FilingHistoryAssociatedFiling();

        when(descriptionValuesMapper.map(any())).thenReturn(descriptionValues);

        // when
        associatedFilingChildMapper.mapChild(request, existingAssociatedFiling);

        // then
        assertEquals(expectedAssociatedFiling, existingAssociatedFiling);
        verify(descriptionValuesMapper).map(requestDescriptionValues);
    }
}
