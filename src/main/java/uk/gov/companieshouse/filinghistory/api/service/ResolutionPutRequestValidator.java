package uk.gov.companieshouse.filinghistory.api.service;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.api.filinghistory.Resolution;

@Component
public class ResolutionPutRequestValidator implements Validator<InternalFilingHistoryApi> {

    @Override
    public boolean isValid(InternalFilingHistoryApi request) {
        ExternalData externalData = request.getExternalData();
        InternalData internalData = request.getInternalData();
        if (externalData == null || internalData == null) {
            return false;
        }

        List<Resolution> resolutionList = externalData.getResolutions();
        if(resolutionList == null || resolutionList.isEmpty()){
            return false;
        }
        Resolution resolution = resolutionList.getFirst();

        return StringUtils.isNotBlank(internalData.getEntityId())
                && StringUtils.isNotBlank(internalData.getDeltaAt())
                && StringUtils.isNotBlank(internalData.getCompanyNumber())
                && StringUtils.isNotBlank(externalData.getTransactionId())
                && StringUtils.isNotBlank(resolution.getCategory().getValue())
                && StringUtils.isNotBlank(resolution.getDescription())
                && StringUtils.isNotBlank(externalData.getDate())
                && externalData.getLinks() != null
                && StringUtils.isNotBlank(externalData.getLinks().getSelf());
    }
}
