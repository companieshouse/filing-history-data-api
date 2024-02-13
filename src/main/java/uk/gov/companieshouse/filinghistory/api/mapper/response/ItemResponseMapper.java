package uk.gov.companieshouse.filinghistory.api.mapper.response;

import static uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import static uk.gov.companieshouse.api.filinghistory.ExternalData.SubcategoryEnum;

import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@Component
public class ItemResponseMapper {

    private final AnnotationsResponseMapper annotationsResponseMapper;
    private final DescriptionValuesResponseMapper descriptionValuesResponseMapper;
    private final LinksResponseMapper linksResponseMapper;

    public ItemResponseMapper(AnnotationsResponseMapper annotationsResponseMapper, DescriptionValuesResponseMapper descriptionValuesResponseMapper, LinksResponseMapper linksResponseMapper) {
        this.annotationsResponseMapper = annotationsResponseMapper;
        this.descriptionValuesResponseMapper = descriptionValuesResponseMapper;
        this.linksResponseMapper = linksResponseMapper;
    }

    public ExternalData mapFilingHistory(FilingHistoryDocument document) {
        FilingHistoryData data = document.getData();
        return new ExternalData()
                .transactionId(document.getTransactionId())
                .barcode(document.getBarcode())
                .type(data.getType())
                .date(LocalDate.ofInstant(data.getDate(), ZoneOffset.UTC).toString())
                .category(CategoryEnum.fromValue(data.getCategory()))
                .subcategory(SubcategoryEnum.fromValue(data.getSubcategory()))
                .annotations(annotationsResponseMapper.map(data.getAnnotations()))
                .description(data.getDescription())
                .descriptionValues(descriptionValuesResponseMapper.map(data.getDescriptionValues()))
                .pages(data.getPages())
                .actionDate(LocalDate.ofInstant(data.getActionDate(), ZoneOffset.UTC).toString())
                .paperFiled(data.getPaperFiled())
                .links(linksResponseMapper.map(data.getLinks()));
    }
}
