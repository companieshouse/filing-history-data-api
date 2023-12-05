package uk.gov.companieshouse.filinghistory.api.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.logging.util.LogContextProperties.REQUEST_ID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.filinghistory.api.logging.DataMapHolder;
import uk.gov.companieshouse.logging.util.LogContextProperties;

@ExtendWith(MockitoExtension.class)
class RequestLoggingInterceptorTest {

    @InjectMocks
    private RequestLoggingInterceptor requestLoggingInterceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @Mock
    private HttpSession httpSession;

    @Test
    void preHandleShouldInitialiseRequestId() {
        when(request.getHeader(REQUEST_ID.value())).thenReturn("request-id");
        when(request.getSession()).thenReturn(httpSession);

        requestLoggingInterceptor.preHandle(request, response, handler);

        Assertions.assertEquals("request-id", DataMapHolder.getRequestId());
    }

    @Test
    void postHandleShouldNotClearDataMapHolder() {
        DataMapHolder.initialise("request-id");
        requestLoggingInterceptor.postHandle(request, response, handler, null);

        assertEquals("request-id", DataMapHolder.getRequestId());
    }

    @Test
    void afterCompletionShouldClearDataMapHolder() throws Exception {
        when(request.getSession()).thenReturn(httpSession);
        when(httpSession.getAttribute(LogContextProperties.START_TIME_KEY.value()))
                .thenReturn(System.currentTimeMillis());
        DataMapHolder.initialise("request-id");

        requestLoggingInterceptor.afterCompletion(request, response, handler, null);

        assertEquals("uninitialised", DataMapHolder.getRequestId());
    }
}
