package uk.gov.companieshouse.filinghistory.api.mapper.response;

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
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAnnotations;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDescriptionValues;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryLinks;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryOriginalValues;

@ExtendWith(MockitoExtension.class)
class ItemResponseMapperTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String BARCODE = "X4BI89B6";
    private static final String TRANSACTION_ID = "Mkv9213";
    private static final String DESCRIPTION = "termination-director-company-with-name-termination-date";
    private static final String TM01_TYPE = "TM01";

    @InjectMocks
    private ItemResponseMapper itemResponseMapper;

    @Mock
    private AnnotationsResponseMapper annotationsResponseMapper;
    @Mock
    private DescriptionValuesResponseMapper descriptionValuesResponseMapper;
    @Mock
    private LinksResponseMapper linksResponseMapper;

    @Mock
    private List<FilingHistoryItemDataAnnotations> itemAnnotations;
    @Mock
    private FilingHistoryItemDataDescriptionValues itemDescriptionValues;
    @Mock
    private FilingHistoryItemDataLinks itemLinks;

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
                .category(ExternalData.CategoryEnum.OFFICERS)
                .type(TM01_TYPE)
                .description(DESCRIPTION)
                .subcategory(ExternalData.SubcategoryEnum.TERMINATION)
                .date("2014-09-15")
                .descriptionValues(itemDescriptionValues)
                .annotations(itemAnnotations)
                .links(itemLinks)
                .pages(1);

        when(annotationsResponseMapper.map(any())).thenReturn(itemAnnotations);
        when(descriptionValuesResponseMapper.map(any())).thenReturn(itemDescriptionValues);
        when(linksResponseMapper.map(any())).thenReturn(itemLinks);

        // when
        final ExternalData actual = itemResponseMapper.mapFilingHistoryItem(buildFilingHistoryDocument());

        // then
        assertEquals(expected, actual);
        verify(annotationsResponseMapper).map(any());
        verify(descriptionValuesResponseMapper).map(any());
        verify(linksResponseMapper).map(any());
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
                .updatedAt(Instant.now())
                .updatedBy("5419d856b6a59f32b7684d0e")
                .originalValues(new FilingHistoryOriginalValues()
                        .officerName("John Tester")
                        .resignationDate("29/08/2014"))
                .originalDescription("Appointment Terminated, Director john tester")
                .documentId("000X4BI89B65846");
    }
}
