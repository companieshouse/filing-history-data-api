package uk.gov.companieshouse.filinghistory.api.service;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.AssociatedFiling;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

@Component
public class AssociatedFilingPutRequestValidator implements Validator<InternalFilingHistoryApi> {

    @Override
    public boolean isValid(InternalFilingHistoryApi request) {
        ExternalData externalData = request.getExternalData();
        InternalData internalData = request.getInternalData();
        if (externalData == null || internalData == null) {
            return false;
        }

        List<AssociatedFiling> associatedFilingList = externalData.getAssociatedFilings();
        if (associatedFilingList == null || associatedFilingList.isEmpty()) {
            return false;
        }
        AssociatedFiling associatedFiling = associatedFilingList.getFirst();

        return StringUtils.isNotBlank(internalData.getEntityId())
                && StringUtils.isNotBlank(internalData.getDeltaAt())
                && StringUtils.isNotBlank(internalData.getCompanyNumber())
                && StringUtils.isNotBlank(externalData.getTransactionId())
                && StringUtils.isNotBlank(associatedFiling.getType())
                && StringUtils.isNotBlank(associatedFiling.getDate())
                && externalData.getLinks() != null
                && StringUtils.isNotBlank(externalData.getLinks().getSelf());
    }
}
