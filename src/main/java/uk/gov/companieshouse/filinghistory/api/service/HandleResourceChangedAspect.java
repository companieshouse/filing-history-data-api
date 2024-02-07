package uk.gov.companieshouse.filinghistory.api.service;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.filinghistory.api.service.ServiceResult;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Aspect
@Component
@ConditionalOnProperty(prefix = "feature", name = "resource_changed_call.disabled")
public class HandleResourceChangedAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);

    @Around("@annotation(HandleResourceChanged)")
    public ServiceResult resourceChangedCallDisabled() {
            LOGGER.debug("Resource changed calls disabled; not publishing change to chs-kafka-api",
                    DataMapHolder.getLogMap());
            return ServiceResult.UPSERT_SUCCESSFUL;
    }
}
