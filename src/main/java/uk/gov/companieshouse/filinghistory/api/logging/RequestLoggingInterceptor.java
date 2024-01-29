package uk.gov.companieshouse.filinghistory.api.logging;

import static uk.gov.companieshouse.logging.util.LogContextProperties.REQUEST_ID;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.companieshouse.filinghistory.api.FilingHistoryApplication;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.util.RequestLogger;

public class RequestLoggingInterceptor implements HandlerInterceptor, RequestLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            FilingHistoryApplication.NAMESPACE);

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nullable Object handler) {
        logStartRequestProcessing(request, LOGGER);
        DataMapHolder.initialise(Optional
                .ofNullable(request.getHeader(REQUEST_ID.value()))
                .orElse(UUID.randomUUID().toString()));
        return true;
    }

    @Override
    public void postHandle(@Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nullable Object handler, ModelAndView modelAndView) {
        // Np-op
    }

    @Override
    public void afterCompletion(@Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull Object handler, Exception ex) throws Exception {
        logEndRequestProcessing(request, response, LOGGER);
        DataMapHolder.clear();
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
