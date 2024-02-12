package uk.gov.companieshouse.filinghistory.api.mapper;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAnnotations;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;

@Component
public class AnnotationsResponseMapper {

    public List<FilingHistoryItemDataAnnotations> map(List<FilingHistoryAnnotation> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return null;
        }

        List<FilingHistoryItemDataAnnotations> responseAnnotations = new ArrayList<>();
        for (final FilingHistoryAnnotation annotation : annotations) {
            responseAnnotations.add(
                    new FilingHistoryItemDataAnnotations()
                            .annotation(annotation.getAnnotation()));
        }
        return responseAnnotations;
    }
}
