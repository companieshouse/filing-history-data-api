package uk.gov.companieshouse.filinghistory.api.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class FilingHistoryRequestValidator<T> implements Validator<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);

    @Override
    public ServiceResult validate(T input) {
        final InternalFilingHistoryApi requestBody = (InternalFilingHistoryApi) input;

        ExternalData externalData = requestBody.getExternalData();
        InternalData internalData = requestBody.getInternalData();

        if (externalData == null
                || internalData == null
                || StringUtils.isBlank(internalData.getEntityId())
                || StringUtils.isBlank(internalData.getDeltaAt())
                || StringUtils.isBlank(externalData.getTransactionId())
                || externalData.getCategory() == null
                || StringUtils.isBlank(externalData.getType())
                || StringUtils.isBlank(externalData.getDate())
                || StringUtils.isBlank(externalData.getDescription())
                || externalData.getLinks() == null
                || StringUtils.isBlank(externalData.getLinks().getSelf())) {
            LOGGER.error("The request body was missing a required field", DataMapHolder.getLogMap());
            return ServiceResult.BAD_REQUEST;
        }
        return ServiceResult.VALID_REQUEST;
    }
}
