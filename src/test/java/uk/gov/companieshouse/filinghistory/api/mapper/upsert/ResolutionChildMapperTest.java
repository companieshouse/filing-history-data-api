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
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.api.filinghistory.Resolution;
import uk.gov.companieshouse.api.filinghistory.Resolution.CategoryEnum;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryResolution;

@ExtendWith(MockitoExtension.class)
class ResolutionChildMapperTest {

    private static final String ENTITY_ID = "1234567890";
    private static final String NEWEST_REQUEST_DELTA_AT = "20151025185208001000";
    private static final String[] SUBCATEGORY_ARRAY = new String[]{"voluntary", "resolution"};

    @InjectMocks
    private ResolutionChildMapper resolutionChildMapper;

    @Mock
    private DescriptionValuesMapper descriptionValuesMapper;
    @Mock
    private DescriptionValues requestDescriptionValues;
    @Mock
    private FilingHistoryDescriptionValues descriptionValues;

    @Test
    void shouldAddNewResolutionWhenNewObjectPassedInArguements() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT))
                .externalData(new ExternalData()
                        .resolutions(List.of(
                                new Resolution()
                                        .category(CategoryEnum.RESOLUTION)
                                        .date("2011-11-26T11:27:55.000Z")
                                        .description("Resolution description")
                                        .type("Resolution")
                                        .subcategory(SUBCATEGORY_ARRAY)
                                        .descriptionValues(requestDescriptionValues)
                                        .originalDescription("resolution original description")
                                        .barcode("barcode")
                        )));

        FilingHistoryResolution expected = new FilingHistoryResolution()
                .entityId(ENTITY_ID)
                .deltaAt(NEWEST_REQUEST_DELTA_AT)
                .category(CategoryEnum.RESOLUTION.getValue())
                .date(Instant.parse("2011-11-26T11:27:55.000Z"))
                .description("Resolution description")
                .type("Resolution")
                .subcategory(SUBCATEGORY_ARRAY)
                .descriptionValues(descriptionValues)
                .originalDescription("resolution original description")
                .barcode("barcode");

        when(descriptionValuesMapper.map(any())).thenReturn(descriptionValues);

        // when
        FilingHistoryResolution actual = resolutionChildMapper.mapChild(request, new FilingHistoryResolution());

        // then
        assertEquals(expected, actual);
        verify(descriptionValuesMapper).map(requestDescriptionValues);
    }

    @Test
    void shouldUpdateExistingResolution() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT))
                .externalData(new ExternalData()
                        .resolutions(List.of(
                                new Resolution()
                                        .category(CategoryEnum.RESOLUTION)
                                        .date("2011-11-26T11:27:55.000Z")
                                        .description("Resolution description")
                                        .type("Resolution")
                                        .subcategory(SUBCATEGORY_ARRAY)
                                        .descriptionValues(requestDescriptionValues)
                                        .originalDescription("resolution original description")
                                        .barcode("barcode")
                        )));

        FilingHistoryResolution expected = new FilingHistoryResolution()
                .entityId(ENTITY_ID)
                .deltaAt(NEWEST_REQUEST_DELTA_AT)
                .category(CategoryEnum.RESOLUTION.getValue())
                .date(Instant.parse("2011-11-26T11:27:55.000Z"))
                .description("Resolution description")
                .type("Resolution")
                .subcategory(SUBCATEGORY_ARRAY)
                .descriptionValues(descriptionValues)
                .originalDescription("resolution original description")
                .barcode("barcode");

        FilingHistoryResolution existingResolution = new FilingHistoryResolution();

        when(descriptionValuesMapper.map(any())).thenReturn(descriptionValues);


        // when
        resolutionChildMapper.mapChild(request, existingResolution);

        // then
        assertEquals(expected, existingResolution);
        verify(descriptionValuesMapper).map(requestDescriptionValues);
    }

}
