package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAnnotations;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;

@ExtendWith(MockitoExtension.class)
class AnnotationListMapperTest {

    private static final String ENTITY_ID = "1234567890";
    private static final String NEWEST_REQUEST_DELTA_AT = "20151025185208001000";

    @InjectMocks
    private AnnotationListMapper annotationListMapper;

    @Mock
    private DescriptionValuesMapper descriptionValuesMapper;

    @Mock
    private FilingHistoryItemDataDescriptionValues requestDescriptionValues;
    @Mock
    private FilingHistoryDescriptionValues descriptionValues;

    @Test
    void shouldAddNewAnnotationWhenNewListPassedInArgs() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT))
                .externalData(new ExternalData()
                        .annotations(List.of(
                                new FilingHistoryItemDataAnnotations()
                                        .category("annotation")
                                        .annotation("Clarification This document was second filed with the CH04 registered on 26/11/2011")
                                        .description("annotation")
                                        .descriptionValues(requestDescriptionValues)
                                        .type("ANNOTATION")
                                        .date("2011-11-26T11:27:55.000Z")
                        )));

        List<FilingHistoryAnnotation> expected = List.of(
                new FilingHistoryAnnotation()
                        .annotation("Clarification This document was second filed with the CH04 registered on 26/11/2011")
                        .entityId(ENTITY_ID)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT)
                        .category("annotation")
                        .date(Instant.parse("2011-11-26T11:27:55.000Z"))
                        .description("annotation")
                        .descriptionValues(descriptionValues)
                        .type("ANNOTATION")
        );

        when(descriptionValuesMapper.map(any())).thenReturn(descriptionValues);

        // when
        List<FilingHistoryAnnotation> actual = annotationListMapper.addNewChildToList(new ArrayList<>(), request);

        // then
        assertEquals(expected, actual);
        verify(descriptionValuesMapper).map(requestDescriptionValues);
    }

    @Test
    void shouldAddNewAnnotationToExistingListPassedInArgs() {
        // given
        InternalFilingHistoryApi request = new InternalFilingHistoryApi()
                .internalData(new InternalData()
                        .entityId(ENTITY_ID)
                        .deltaAt(NEWEST_REQUEST_DELTA_AT))
                .externalData(new ExternalData()
                        .annotations(List.of(
                                new FilingHistoryItemDataAnnotations()
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

        List<FilingHistoryAnnotation> existingList = new ArrayList<>();
        existingList.add(new FilingHistoryAnnotation());

        List<FilingHistoryAnnotation> expectedList = List.of(
                new FilingHistoryAnnotation(),
                expectedAnnotation
        );

        when(descriptionValuesMapper.map(any())).thenReturn(descriptionValues);

        // when
        annotationListMapper.addNewChildToList(existingList, request);

        // then
        assertEquals(expectedList, existingList);
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
                                new FilingHistoryItemDataAnnotations()
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
        annotationListMapper.updateExistingChild(existingAnnotation, request);

        // then
        assertEquals(expectedAnnotation, existingAnnotation);
        verify(descriptionValuesMapper).map(requestDescriptionValues);
    }
}
