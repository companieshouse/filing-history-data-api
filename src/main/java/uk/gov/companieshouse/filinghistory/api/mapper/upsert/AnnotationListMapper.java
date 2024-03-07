package uk.gov.companieshouse.filinghistory.api.mapper.upsert;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.model.FilingHistoryAnnotation;

@Component
public class AnnotationListMapper {

    public List<FilingHistoryAnnotation> mapAnnotations(List<FilingHistoryAnnotation> annotationsList) {
        return annotationsList;
    }
}
