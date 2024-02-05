package uk.gov.companieshouse.filinghistory.api.client;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Aspect
@Component
@ConditionalOnProperty(prefix = "feature", name = "resource_changed_call.enabled")
public class ResourceChangedApiServiceAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);

    @Around("@annotation(StreamEvents)")
    public Object checkStreamEventsEnabled() {
            LOGGER.debug("Stream events disabled; not publishing change to chs-kafka-api",
                    DataMapHolder.getLogMap());
            return null;
    }
}
