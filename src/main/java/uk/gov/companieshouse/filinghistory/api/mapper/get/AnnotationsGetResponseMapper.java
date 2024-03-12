package uk.gov.companieshouse.filinghistory.api.mapper.get;

import static uk.gov.companieshouse.filinghistory.api.mapper.DateUtils.instantToString;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAnnotations;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;

@Component
public class AnnotationsGetResponseMapper {

    private final DescriptionValuesGetResponseMapper mapper;

    public AnnotationsGetResponseMapper(DescriptionValuesGetResponseMapper mapper) {
        this.mapper = mapper;
    }

    public List<FilingHistoryItemDataAnnotations> map(List<FilingHistoryAnnotation> documentAnnotations) {
        return Optional.ofNullable(documentAnnotations)
                .map(inputAnnotations -> inputAnnotations
                        .stream()
                        .map(annotation ->
                                new FilingHistoryItemDataAnnotations()
                                        .annotation(annotation.getAnnotation())
                                        .category(annotation.getCategory())
                                        .description(annotation.getDescription())
                                        .type(annotation.getType())
                                        .date(instantToString(annotation.getDate()))
                                        .descriptionValues(mapper.map(annotation.getDescriptionValues())))
                        .toList())
                .orElse(null);
    }
}
