package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.stringToInstant;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAssociatedFilings;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAssociatedFiling;

@Component
public class AssociatedFilingListMapper implements ChildListMapper<FilingHistoryAssociatedFiling> {

    private final DescriptionValuesMapper descriptionValuesMapper;

    public AssociatedFilingListMapper(DescriptionValuesMapper descriptionValuesMapper) {
        this.descriptionValuesMapper = descriptionValuesMapper;
    }

    @Override
    public FilingHistoryAssociatedFiling mapChild(FilingHistoryAssociatedFiling associatedFiling,
                                                  InternalFilingHistoryApi request) {
        InternalData internalData = request.getInternalData();
        FilingHistoryItemDataAssociatedFilings requestAssociatedFilings =
                request.getExternalData().getAssociatedFilings().getFirst();

        return associatedFiling
                .entityId(internalData.getEntityId())
                .deltaAt(internalData.getDeltaAt())
                .category(requestAssociatedFilings.getCategory())
                .date(stringToInstant(requestAssociatedFilings.getDate()))
                .description(requestAssociatedFilings.getDescription())
                .descriptionValues(descriptionValuesMapper.map(requestAssociatedFilings.getDescriptionValues()))
                .type(requestAssociatedFilings.getType());
    }
}
