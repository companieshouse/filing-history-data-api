package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.instantToString;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryData;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryDocument;

@Component
public class ItemGetResponseMapper {

    private static final String TOP_LEVEL_ANNOTATION_TYPE = "ANNOTATION";

    private final AnnotationsGetResponseMapper annotationsGetResponseMapper;
    private final ResolutionsGetResponseMapper resolutionsGetResponseMapper;
    private final AssociatedFilingsGetResponseMapper associatedFilingsGetResponseMapper;
    private final DescriptionValuesGetResponseMapper descriptionValuesGetResponseMapper;
    private final LinksGetResponseMapper linksGetResponseMapper;

    public ItemGetResponseMapper(AnnotationsGetResponseMapper annotationsGetResponseMapper,
            ResolutionsGetResponseMapper resolutionsGetResponseMapper,
            AssociatedFilingsGetResponseMapper associatedFilingsGetResponseMapper,
            DescriptionValuesGetResponseMapper descriptionValuesGetResponseMapper,
            LinksGetResponseMapper linksGetResponseMapper) {
        this.annotationsGetResponseMapper = annotationsGetResponseMapper;
        this.resolutionsGetResponseMapper = resolutionsGetResponseMapper;
        this.associatedFilingsGetResponseMapper = associatedFilingsGetResponseMapper;
        this.descriptionValuesGetResponseMapper = descriptionValuesGetResponseMapper;
        this.linksGetResponseMapper = linksGetResponseMapper;
    }

    public ExternalData mapFilingHistoryItem(FilingHistoryDocument document) {
        final FilingHistoryData data = document.getData();
        return new ExternalData()
                .transactionId(document.getTransactionId())
                .barcode(document.getBarcode())
                .type(data.getType())
                .date(instantToString(data.getDate()))
                .category(data.getCategory() == null ? null : CategoryEnum.fromValue(data.getCategory()))
                .subcategory(data.getSubcategory())
                .annotations(TOP_LEVEL_ANNOTATION_TYPE.equals(data.getType()) ?
                        null : annotationsGetResponseMapper.map(data.getAnnotations()))
                .resolutions(resolutionsGetResponseMapper.map(data.getResolutions()))
                .associatedFilings(associatedFilingsGetResponseMapper.map(data.getAssociatedFilings()))
                .description(data.getDescription())
                .descriptionValues(descriptionValuesGetResponseMapper.map(data.getDescriptionValues()))
                .pages(data.getPages())
                .actionDate(instantToString(data.getActionDate()))
                .paperFiled(data.getPaperFiled())
                .links(linksGetResponseMapper.map(data.getLinks()));
    }
}
