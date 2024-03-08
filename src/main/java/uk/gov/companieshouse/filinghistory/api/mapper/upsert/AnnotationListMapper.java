package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;

@Component
public class AnnotationListMapper {

    public List<FilingHistoryAnnotation> addNewAnnotationToList(List<FilingHistoryAnnotation> annotationsList) {
        return annotationsList;
    }

    public void updateExistingAnnotation(FilingHistoryAnnotation annotation) {
        // Update existing annotation
    }
}
