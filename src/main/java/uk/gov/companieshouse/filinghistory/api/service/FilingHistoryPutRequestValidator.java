package uk.gov.companieshouse.filinghistory.api.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

@Component
public class FilingHistoryPutRequestValidator<T> implements Validator<T> {

    @Override
    public boolean isValid(T input) {
        final InternalFilingHistoryApi requestBody = (InternalFilingHistoryApi) input;

        ExternalData externalData = requestBody.getExternalData();
        InternalData internalData = requestBody.getInternalData();

        return externalData != null
                && internalData != null
                && !StringUtils.isBlank(internalData.getEntityId())
                && !StringUtils.isBlank(internalData.getDeltaAt())
                && !StringUtils.isBlank(internalData.getCompanyNumber())
                && !StringUtils.isBlank(externalData.getTransactionId())
                && externalData.getCategory() != null // NOSONAR
                && !StringUtils.isBlank(externalData.getType())
                && !StringUtils.isBlank(externalData.getDate())
                && !StringUtils.isBlank(externalData.getDescription())
                && externalData.getLinks() != null
                && !StringUtils.isBlank(externalData.getLinks().getSelf());
    }
}
