package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static uk.gov.companieshouse.filinghistory.api.mapper.get.DateUtils.convertInstantToLocalDateString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAssociatedFilings;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAssociatedFiling;

@Component
public class AssociatedFilingsGetResponseMapper {

    private final DescriptionValuesGetResponseMapper descriptionValuesGetResponseMapper;

    public AssociatedFilingsGetResponseMapper(DescriptionValuesGetResponseMapper descriptionValuesGetResponseMapper) {
        this.descriptionValuesGetResponseMapper = descriptionValuesGetResponseMapper;
    }

    public List<FilingHistoryItemDataAssociatedFilings> map(List<FilingHistoryAssociatedFiling> documentAssociatedFilings) {
        Optional<List<FilingHistoryAssociatedFiling>> optionalDocumentAssociatedFilings = Optional.ofNullable(documentAssociatedFilings);

        return optionalDocumentAssociatedFilings.map(inputAssociatedFilings -> {
            List<FilingHistoryItemDataAssociatedFilings> outputAssociatedFilings = new ArrayList<>();
            inputAssociatedFilings.forEach(associatedFiling ->
                    outputAssociatedFilings.add(
                            new FilingHistoryItemDataAssociatedFilings()
                                    .category(associatedFiling.getCategory())
                                    .type(associatedFiling.getType())
                                    .description(associatedFiling.getDescription())
                                    .date(convertInstantToLocalDateString(associatedFiling.getDate()))
                                    .descriptionValues(descriptionValuesGetResponseMapper.map(associatedFiling.getDescriptionValues()))));
            return outputAssociatedFilings;
        }).orElse(null);
    }
}
