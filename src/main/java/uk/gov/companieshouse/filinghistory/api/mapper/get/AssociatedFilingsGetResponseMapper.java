package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.instantToString;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAssociatedFilings;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAssociatedFiling;

@Component
public class AssociatedFilingsGetResponseMapper {

    private final DescriptionValuesGetResponseMapper mapper;

    public AssociatedFilingsGetResponseMapper(DescriptionValuesGetResponseMapper mapper) {
        this.mapper = mapper;
    }

    public List<FilingHistoryItemDataAssociatedFilings> map(List<FilingHistoryAssociatedFiling> documentAssociatedFilings) {
        return Optional.ofNullable(documentAssociatedFilings)
                .map(inputAssociatedFilings -> inputAssociatedFilings
                        .stream()
                        .map(associatedFiling ->
                                new FilingHistoryItemDataAssociatedFilings()
                                        .category(associatedFiling.getCategory())
                                        .type(associatedFiling.getType())
                                        .description(associatedFiling.getDescription())
                                        .date(instantToString(associatedFiling.getDate()))
                                        .descriptionValues(mapper.map(associatedFiling.getDescriptionValues())))
                        .toList())
                .orElse(null);
    }
}
