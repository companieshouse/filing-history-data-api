package uk.gov.companieshouse.filinghistory.api.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

@Component
public class TopLevelPutRequestValidator implements Validator<InternalFilingHistoryApi> {

    @Override
    public boolean isValid(InternalFilingHistoryApi request) {
        ExternalData externalData = request.getExternalData();
        InternalData internalData = request.getInternalData();

        return externalData != null
                && internalData != null
                && StringUtils.isNotBlank(internalData.getEntityId())
                && StringUtils.isNotBlank(internalData.getDeltaAt())
                && StringUtils.isNotBlank(internalData.getCompanyNumber())
                && StringUtils.isNotBlank(externalData.getTransactionId())
                && externalData.getCategory() != null // NOSONAR
                && StringUtils.isNotBlank(externalData.getType())
                && StringUtils.isNotBlank(externalData.getDate())
                && StringUtils.isNotBlank(externalData.getDescription())
                && externalData.getLinks() != null
                && StringUtils.isNotBlank(externalData.getLinks().getSelf());
    }
}
