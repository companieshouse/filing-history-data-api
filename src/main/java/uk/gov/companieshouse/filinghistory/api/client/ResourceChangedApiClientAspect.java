package uk.gov.companieshouse.filinghistory.api.client;

import java.util.Map;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Aspect
@Component
@ConditionalOnProperty(prefix = "feature", name = "resource_changed_call.disabled", havingValue = "true")
public class ResourceChangedApiClientAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilingHistoryApplication.NAMESPACE);

    @Around("@annotation(CallResourceChanged)")
    public ApiResponse<Void> callResourceChangedDisabled() {
        LOGGER.debug("Resource changed call disabled; not publishing change to chs-kafka-api",
                DataMapHolder.getLogMap());
        return new ApiResponse<>(200, Map.of());
    }
}
