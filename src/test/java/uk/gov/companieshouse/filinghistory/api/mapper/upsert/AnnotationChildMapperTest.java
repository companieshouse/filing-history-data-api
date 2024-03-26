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
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.Annotation;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDescriptionValues;

@ExtendWith(MockitoExtension.class)
class AnnotationChildMapperTest {

    private static final String ENTITY_ID = "1234567890";
    private static final String NEWEST_REQUEST_DELTA_AT = "20151025185208001000";

    @InjectMocks
    private AnnotationChildMapper annotationChildMapper;

    @Mock
    private DescriptionValuesMapper descriptionValuesMapper;

    @Mock
    private DescriptionValues requestDescriptionValues;
    @Mock
    private FilingHistoryDescriptionValues descriptionValues;

    @Test
    void shouldAddNewAnnotationWhenNewObjectPassedInArgs() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT))
                .externalData(new ExternalData()
                        .annotations(List.of(
                                new Annotation()
                                        .category("annotation")
                                        .annotation("Clarification This document was second filed with the CH04 registered on 26/11/2011")
                                        .description("annotation")
                                        .descriptionValues(requestDescriptionValues)
                                        .type("ANNOTATION")
                                        .date("2011-11-26T11:27:55.000Z")
                        )));

        FilingHistoryAnnotation expected = new FilingHistoryAnnotation()
                .annotation("Clarification This document was second filed with the CH04 registered on 26/11/2011")
                .entityId(ENTITY_ID)
                .deltaAt(NEWEST_REQUEST_DELTA_AT)
                .category("annotation")
                .date(Instant.parse("2011-11-26T11:27:55.000Z"))
                .description("annotation")
                .descriptionValues(descriptionValues)
                .type("ANNOTATION");

        when(descriptionValuesMapper.map(any())).thenReturn(descriptionValues);

        // when
        FilingHistoryAnnotation actual = annotationChildMapper.mapChild(new FilingHistoryAnnotation(), request);

        // then
        assertEquals(expected, actual);
        verify(descriptionValuesMapper).map(requestDescriptionValues);
    }

    @Test
    void shouldUpdateExistingAnnotation() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT))
                .externalData(new ExternalData()
                        .annotations(List.of(
                                new Annotation()
                                        .category("annotation")
                                        .annotation("Clarification This document was second filed with the CH04 registered on 26/11/2011")
                                        .description("annotation")
                                        .descriptionValues(requestDescriptionValues)
                                        .type("ANNOTATION")
                                        .date("2011-11-26T11:27:55.000Z")
                        )));


        FilingHistoryAnnotation expectedAnnotation = new FilingHistoryAnnotation()
                .annotation("Clarification This document was second filed with the CH04 registered on 26/11/2011")
                .entityId(ENTITY_ID)
                .deltaAt(NEWEST_REQUEST_DELTA_AT)
                .category("annotation")
                .date(Instant.parse("2011-11-26T11:27:55.000Z"))
                .description("annotation")
                .descriptionValues(descriptionValues)
                .type("ANNOTATION");

        FilingHistoryAnnotation existingAnnotation = new FilingHistoryAnnotation();

        when(descriptionValuesMapper.map(any())).thenReturn(descriptionValues);

        // when
        annotationChildMapper.mapChild(existingAnnotation, request);

        // then
        assertEquals(expectedAnnotation, existingAnnotation);
        verify(descriptionValuesMapper).map(requestDescriptionValues);
    }
}
