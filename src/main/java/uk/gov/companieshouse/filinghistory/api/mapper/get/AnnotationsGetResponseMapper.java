package uk.gov.companieshouse.filinghistory.api.mapper.get;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAnnotations;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;

@Component
public class AnnotationsGetResponseMapper {

    public List<FilingHistoryItemDataAnnotations> map(List<FilingHistoryAnnotation> documentAnnotations) {

        Optional<List<FilingHistoryAnnotation>> optionalDocumentAnnotations = Optional.ofNullable(documentAnnotations);

        return optionalDocumentAnnotations.map(inputAnnotations -> {
            List<FilingHistoryItemDataAnnotations> outputAnnotations = new ArrayList<>();
            inputAnnotations.forEach(annotation ->
                    outputAnnotations.add(
                            new FilingHistoryItemDataAnnotations()
                                    .annotation(annotation.getAnnotation())));
            return outputAnnotations;
        }).orElse(null);
    }
}
