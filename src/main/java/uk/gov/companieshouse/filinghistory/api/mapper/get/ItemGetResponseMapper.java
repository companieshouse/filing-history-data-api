package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import static uk.gov.companieshouse.api.filinghistory.ExternalData.SubcategoryEnum;

import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryDocument;

@Component
public class ItemGetResponseMapper {

    private final AnnotationsGetResponseMapper annotationsGetResponseMapper;
    private final DescriptionGetValuesResponseMapper descriptionGetValuesResponseMapper;
    private final LinksGetResponseMapper linksGetResponseMapper;

    public ItemGetResponseMapper(AnnotationsGetResponseMapper annotationsGetResponseMapper, DescriptionGetValuesResponseMapper descriptionGetValuesResponseMapper, LinksGetResponseMapper linksGetResponseMapper) {
        this.annotationsGetResponseMapper = annotationsGetResponseMapper;
        this.descriptionGetValuesResponseMapper = descriptionGetValuesResponseMapper;
        this.linksGetResponseMapper = linksGetResponseMapper;
    }

    public ExternalData mapFilingHistoryItem(FilingHistoryDocument document) {
        FilingHistoryData data = document.getData();
        return new ExternalData()
                .transactionId(document.getTransactionId())
                .barcode(document.getBarcode())
                .type(data.getType())
                .date(LocalDate.ofInstant(data.getDate(), ZoneOffset.UTC).toString())
                .category(CategoryEnum.fromValue(data.getCategory()))
                .subcategory(SubcategoryEnum.fromValue(data.getSubcategory()))
                .annotations(annotationsGetResponseMapper.map(data.getAnnotations()))
                .description(data.getDescription())
                .descriptionValues(descriptionGetValuesResponseMapper.map(data.getDescriptionValues()))
                .pages(data.getPages())
                .actionDate(LocalDate.ofInstant(data.getActionDate(), ZoneOffset.UTC).toString())
                .paperFiled(data.getPaperFiled())
                .links(linksGetResponseMapper.map(data.getLinks()));
    }
}
