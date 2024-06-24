package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.stringToInstant;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.AssociatedFiling;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.mongo.FilingHistoryAssociatedFiling;

@Component
public class AssociatedFilingChildMapper implements ChildMapper<FilingHistoryAssociatedFiling> {

    private final DescriptionValuesMapper descriptionValuesMapper;

    public AssociatedFilingChildMapper(DescriptionValuesMapper descriptionValuesMapper) {
        this.descriptionValuesMapper = descriptionValuesMapper;
    }

    @Override
    public FilingHistoryAssociatedFiling mapChild(InternalFilingHistoryApi request,
            FilingHistoryAssociatedFiling existingAssociatedFiling) {
        InternalData internalData = request.getInternalData();
        AssociatedFiling requestAssociatedFiling =
                request.getExternalData().getAssociatedFilings().getFirst();

        return existingAssociatedFiling
                .actionDate(stringToInstant(requestAssociatedFiling.getActionDate()))
                .entityId(internalData.getEntityId())
                .deltaAt(internalData.getDeltaAt())
                .category(requestAssociatedFiling.getCategory())
                .subcategory(requestAssociatedFiling.getSubcategory())
                .date(stringToInstant(requestAssociatedFiling.getDate()))
                .description(requestAssociatedFiling.getDescription())
                .descriptionValues(descriptionValuesMapper.map(requestAssociatedFiling.getDescriptionValues()))
                .type(requestAssociatedFiling.getType());
    }

    @Override
    public FilingHistoryAssociatedFiling newInstance() {
        return new FilingHistoryAssociatedFiling();
    }
}
