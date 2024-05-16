package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum.OFFICERS;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.Annotation;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.Links;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDeltaTimestamp;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryLinks;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryOriginalValues;

@ExtendWith(MockitoExtension.class)
class ItemGetResponseMapperTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String BARCODE = "X4BI89B6";
    private static final String TRANSACTION_ID = "Mkv9213";
    private static final String DESCRIPTION = "termination-director-company-with-name-termination-date";
    private static final String TM01_TYPE = "TM01";
    private static final String ANNOTATION_TYPE = "ANNOTATION";
    private static final String SUBCATEGORY = "termination";

    @InjectMocks
    private ItemGetResponseMapper itemGetResponseMapper;

    @Mock
    private AnnotationsGetResponseMapper annotationsGetResponseMapper;
    @Mock
    private AssociatedFilingsGetResponseMapper associatedFilingsGetResponseMapper;
    @Mock
    private ResolutionsGetResponseMapper resolutionsGetResponseMapper;
    @Mock
    private DescriptionValuesGetResponseMapper descriptionValuesGetResponseMapper;
    @Mock
    private LinksGetResponseMapper linksGetResponseMapper;

    @Mock
    private List<Annotation> itemAnnotations;
    @Mock
    private DescriptionValues itemDescriptionValues;
    @Mock
    private Links itemLinks;

    @Mock
    private FilingHistoryDescriptionValues documentDescriptionValues;
    @Mock
    private List<FilingHistoryAnnotation> documentAnnotations;
    @Mock
    private FilingHistoryLinks documentLinks;

    @Test
    void shouldSuccessfullyMapDocumentToExternalData() {
        // given
        final ExternalData expected = new ExternalData()
                .transactionId(TRANSACTION_ID)
                .barcode(BARCODE)
                .actionDate("2014-09-15")
                .category(OFFICERS)
                .type(TM01_TYPE)
                .description(DESCRIPTION)
                .subcategory(SUBCATEGORY)
                .date("2014-09-15")
                .descriptionValues(itemDescriptionValues)
                .annotations(itemAnnotations)
                .links(itemLinks)
                .pages(1);

        when(annotationsGetResponseMapper.map(any())).thenReturn(itemAnnotations);
        when(associatedFilingsGetResponseMapper.map(any())).thenReturn(null);
        when(resolutionsGetResponseMapper.map(any())).thenReturn(null);
        when(descriptionValuesGetResponseMapper.map(any())).thenReturn(itemDescriptionValues);
        when(linksGetResponseMapper.map(any())).thenReturn(itemLinks);

        // when
        final ExternalData actual = itemGetResponseMapper.mapFilingHistoryItem(buildFilingHistoryDocument());

        // then
        assertEquals(expected, actual);
        verify(annotationsGetResponseMapper).map(any());
        verify(descriptionValuesGetResponseMapper).map(any());
        verify(linksGetResponseMapper).map(any());
    }

    @Test
    void shouldSuccessfullyMapDocumentToExternalDataWhenDescriptionValuesIsNull() {
        // given
        final ExternalData expected = new ExternalData()
                .transactionId(TRANSACTION_ID)
                .barcode(BARCODE)
                .actionDate("2014-09-15")
                .category(OFFICERS)
                .type(TM01_TYPE)
                .description(DESCRIPTION)
                .subcategory(SUBCATEGORY)
                .date("2014-09-15")
                .annotations(itemAnnotations)
                .links(itemLinks)
                .pages(1);

        when(annotationsGetResponseMapper.map(any())).thenReturn(itemAnnotations);
        when(associatedFilingsGetResponseMapper.map(any())).thenReturn(null);
        when(resolutionsGetResponseMapper.map(any())).thenReturn(null);
        when(linksGetResponseMapper.map(any())).thenReturn(itemLinks);

        // when
        final ExternalData actual = itemGetResponseMapper.mapFilingHistoryItem(
                new FilingHistoryDocument()
                        .transactionId(TRANSACTION_ID)
                        .companyNumber(COMPANY_NUMBER)
                        .data(new FilingHistoryData()
                                .actionDate(Instant.parse("2014-09-15T23:21:18.000Z"))
                                .category("officers")
                                .type(TM01_TYPE)
                                .description(DESCRIPTION)
                                .subcategory("termination")
                                .date(Instant.parse("2014-09-15T23:21:18.000Z"))
                                .descriptionValues(null)
                                .annotations(documentAnnotations)
                                .links(documentLinks)
                                .pages(1))
                        .barcode(BARCODE)
                        .deltaAt("20140815230459600643")
                        .entityId("1234567890")
                        .updated(new FilingHistoryDeltaTimestamp()
                                .at(Instant.now())
                                .by("5419d856b6a59f32b7684d0e"))
                        .originalValues(new FilingHistoryOriginalValues()
                                .officerName("John Tester")
                                .resignationDate("29/08/2014"))
                        .originalDescription("Appointment Terminated, Director john tester")
                        .documentId("000X4BI89B65846"));

        // then
        assertEquals(expected, actual);
        verify(annotationsGetResponseMapper).map(documentAnnotations);
        verify(descriptionValuesGetResponseMapper).map(null);
        verify(linksGetResponseMapper).map(documentLinks);
    }

    @Test
    void shouldSuccessfullyMapDocumentToExternalDataWhenCategoriesAreNull() {
        // given
        final ExternalData expected = new ExternalData()
                .transactionId(TRANSACTION_ID)
                .barcode(BARCODE)
                .actionDate("2014-09-15")
                .type(TM01_TYPE)
                .description(DESCRIPTION)
                .date("2014-09-15")
                .descriptionValues(itemDescriptionValues)
                .annotations(itemAnnotations)
                .links(itemLinks)
                .pages(1);

        when(annotationsGetResponseMapper.map(any())).thenReturn(itemAnnotations);
        when(associatedFilingsGetResponseMapper.map(any())).thenReturn(null);
        when(resolutionsGetResponseMapper.map(any())).thenReturn(null);
        when(descriptionValuesGetResponseMapper.map(any())).thenReturn(itemDescriptionValues);
        when(linksGetResponseMapper.map(any())).thenReturn(itemLinks);

        // when
        final ExternalData actual = itemGetResponseMapper.mapFilingHistoryItem(
                buildFilingHistoryDocumentWithNullCategories());

        // then
        assertEquals(expected, actual);
        verify(annotationsGetResponseMapper).map(any());
        verify(descriptionValuesGetResponseMapper).map(any());
        verify(linksGetResponseMapper).map(any());
    }

    @Test
    void shouldSuccessfullyMapDocumentToExternalDataWithoutAnnotationsListWhenDocumentIsTopLevelAnnotation() {
        // given
        final ExternalData expected = new ExternalData()
                .transactionId(TRANSACTION_ID)
                .barcode(BARCODE)
                .actionDate("2014-09-15")
                .category(OFFICERS)
                .type(ANNOTATION_TYPE)
                .description(DESCRIPTION)
                .subcategory(SUBCATEGORY)
                .date("2014-09-15")
                .descriptionValues(itemDescriptionValues)
                .annotations(null)
                .links(itemLinks)
                .pages(1);

        FilingHistoryDocument document = buildFilingHistoryDocument();
        document.getData().type(ANNOTATION_TYPE);

        when(associatedFilingsGetResponseMapper.map(any())).thenReturn(null);
        when(resolutionsGetResponseMapper.map(any())).thenReturn(null);
        when(descriptionValuesGetResponseMapper.map(any())).thenReturn(itemDescriptionValues);
        when(linksGetResponseMapper.map(any())).thenReturn(itemLinks);

        // when
        final ExternalData actual = itemGetResponseMapper.mapFilingHistoryItem(document);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(annotationsGetResponseMapper);
        verify(descriptionValuesGetResponseMapper).map(any());
        verify(linksGetResponseMapper).map(any());
    }

    private FilingHistoryDocument buildFilingHistoryDocument() {
        return new FilingHistoryDocument()
                .transactionId(TRANSACTION_ID)
                .companyNumber(COMPANY_NUMBER)
                .data(new FilingHistoryData()
                        .actionDate(Instant.parse("2014-09-15T23:21:18.000Z"))
                        .category("officers")
                        .type(TM01_TYPE)
                        .description(DESCRIPTION)
                        .subcategory("termination")
                        .date(Instant.parse("2014-09-15T23:21:18.000Z"))
                        .descriptionValues(documentDescriptionValues)
                        .annotations(documentAnnotations)
                        .links(documentLinks)
                        .pages(1))
                .barcode(BARCODE)
                .deltaAt("20140815230459600643")
                .entityId("1234567890")
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(Instant.now())
                        .by("5419d856b6a59f32b7684d0e"))
                .originalValues(new FilingHistoryOriginalValues()
                        .officerName("John Tester")
                        .resignationDate("29/08/2014"))
                .originalDescription("Appointment Terminated, Director john tester")
                .documentId("000X4BI89B65846");
    }

    private FilingHistoryDocument buildFilingHistoryDocumentWithNullCategories() {
        return new FilingHistoryDocument()
                .transactionId(TRANSACTION_ID)
                .companyNumber(COMPANY_NUMBER)
                .data(new FilingHistoryData()
                        .actionDate(Instant.parse("2014-09-15T23:21:18.000Z"))
                        .type(TM01_TYPE)
                        .description(DESCRIPTION)
                        .date(Instant.parse("2014-09-15T23:21:18.000Z"))
                        .descriptionValues(documentDescriptionValues)
                        .annotations(documentAnnotations)
                        .links(documentLinks)
                        .pages(1))
                .barcode(BARCODE)
                .deltaAt("20140815230459600643")
                .entityId("1234567890")
                .updated(new FilingHistoryDeltaTimestamp()
                        .at(Instant.now())
                        .by("5419d856b6a59f32b7684d0e"))
                .originalValues(new FilingHistoryOriginalValues()
                        .officerName("John Tester")
                        .resignationDate("29/08/2014"))
                .originalDescription("Appointment Terminated, Director john tester")
                .documentId("000X4BI89B65846");
    }
}
