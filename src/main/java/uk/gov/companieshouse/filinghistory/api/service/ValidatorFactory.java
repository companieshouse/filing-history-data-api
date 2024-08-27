package uk.gov.companieshouse.filinghistory.api.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

@Component
public class ValidatorFactory {

    private final TopLevelPutRequestValidator topLevelPutRequestValidator;
    private final AnnotationPutRequestValidator annotationPutRequestValidator;
    private final AssociatedFilingPutRequestValidator associatedFilingPutRequestValidator;
    private final ResolutionPutRequestValidator resolutionPutRequestValidator;

    public ValidatorFactory(TopLevelPutRequestValidator topLevelPutRequestValidator,
                            AnnotationPutRequestValidator annotationPutRequestValidator,
                            AssociatedFilingPutRequestValidator associatedFilingPutRequestValidator,
                            ResolutionPutRequestValidator resolutionPutRequestValidator) {
        this.topLevelPutRequestValidator = topLevelPutRequestValidator;
        this.annotationPutRequestValidator = annotationPutRequestValidator;
        this.associatedFilingPutRequestValidator = associatedFilingPutRequestValidator;
        this.resolutionPutRequestValidator = resolutionPutRequestValidator;
    }

    public Validator<InternalFilingHistoryApi> getPutRequestValidator(TransactionKindEnum kind) {
        return switch (kind) {
            case TOP_LEVEL -> topLevelPutRequestValidator;
            case ANNOTATION -> annotationPutRequestValidator;
            case ASSOCIATED_FILING -> associatedFilingPutRequestValidator;
            case RESOLUTION -> resolutionPutRequestValidator;
        };
    }
}
