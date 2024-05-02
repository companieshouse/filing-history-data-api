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
        AssociatedFiling requestAssociatedFilings =
                request.getExternalData().getAssociatedFilings().getFirst();

        return existingAssociatedFiling
                .entityId(internalData.getEntityId())
                .originalDescription(requestAssociatedFilings.getOriginalDescription())
                .deltaAt(internalData.getDeltaAt())
                .category(requestAssociatedFilings.getCategory())
                .date(stringToInstant(requestAssociatedFilings.getDate()))
                .description(requestAssociatedFilings.getDescription())
                .descriptionValues(descriptionValuesMapper.map(requestAssociatedFilings.getDescriptionValues()))
                .type(requestAssociatedFilings.getType());
    }

    @Override
    public FilingHistoryAssociatedFiling newInstance() {
        return new FilingHistoryAssociatedFiling();
    }
}
