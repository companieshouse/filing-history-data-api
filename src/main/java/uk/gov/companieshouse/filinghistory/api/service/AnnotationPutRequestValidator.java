package uk.gov.companieshouse.filinghistory.api.service;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAnnotations;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

@Component
public class AnnotationPutRequestValidator implements Validator<InternalFilingHistoryApi> {

    @Override
    public boolean isValid(InternalFilingHistoryApi request) {
        ExternalData externalData = request.getExternalData();
        InternalData internalData = request.getInternalData();
        if (externalData == null || internalData == null) {
            return false;
        }

        List<FilingHistoryItemDataAnnotations> annotationList = externalData.getAnnotations();
        if (annotationList == null || annotationList.isEmpty()) {
            return false;
        }
        FilingHistoryItemDataAnnotations annotation = annotationList.getFirst();

        return annotation != null
                && StringUtils.isNotBlank(internalData.getEntityId())
                && StringUtils.isNotBlank(internalData.getDeltaAt())
                && StringUtils.isNotBlank(internalData.getCompanyNumber())
                && StringUtils.isNotBlank(externalData.getTransactionId())
                && StringUtils.isNotBlank(annotation.getAnnotation())
                && StringUtils.isNotBlank(annotation.getType())
                && StringUtils.isNotBlank(annotation.getDate())
                && externalData.getLinks() != null
                && StringUtils.isNotBlank(externalData.getLinks().getSelf());
    }
}
