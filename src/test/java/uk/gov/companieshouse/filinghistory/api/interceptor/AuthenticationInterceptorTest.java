package uk.gov.companieshouse.filinghistory.api.interceptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AuthenticationInterceptorTest {

    private static final String USER = "user";
    private static final String STREAM = "stream";

    @InjectMocks
    private AuthenticationInterceptor authenticationInterceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @Test
    void preHandleReturnsFalseIfEricIdentityIsNull() {
        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(401);
    }

    @Test
    void preHandleReturnsFalseIfEricIdentityIsEmpty() {
        // given
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY_TYPE)).thenReturn(null);
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY)).thenReturn("");

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(401);
    }

    @Test
    void preHandleReturnsFalseIfEricIdentityTypeIsNull() {
        // given
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY_TYPE)).thenReturn(null);
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY)).thenReturn(USER);
        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(401);
    }

    @Test
    void preHandleReturnsFalseIfEricIdentityTypeIsEmpty() {
        // given
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY)).thenReturn(USER);
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY_TYPE)).thenReturn("");

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(401);
    }

    @Test
    void preHandleReturnsFalseIfEricIdentityTypeIsInvalid() {
        // given
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY)).thenReturn(USER);
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY_TYPE)).thenReturn(STREAM);

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(401);
    }

    @Test
    void preHandleReturnsTrueIfEricIdentitySetAndIdentityTypeKey() {
        // given
        when(request.getMethod()).thenReturn(HttpMethod.GET.name());
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY)).thenReturn(USER);
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY_TYPE)).thenReturn(
                AuthenticationConstants.API_KEY_IDENTITY_TYPE);

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertTrue(actual);
        verifyNoInteractions(response);
    }

    @Test
    void preHandleReturnsTrueIfEricIdentitySetAndIdentityTypeOAuth() {
        // given
        when(request.getMethod()).thenReturn(HttpMethod.GET.name());
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY)).thenReturn(USER);
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY_TYPE)).thenReturn(
                AuthenticationConstants.OAUTH2_IDENTITY_TYPE);

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertTrue(actual);
        verifyNoInteractions(response);
    }

    @Test
    void preHandleReturnsTrueIfMethodNotGetAndHasInternalPrivileges() {
        // given
        when(request.getMethod()).thenReturn(HttpMethod.PATCH.name());
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY)).thenReturn(USER);
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY_TYPE)).thenReturn(
                AuthenticationConstants.API_KEY_IDENTITY_TYPE);
        when(request.getHeader(
                AuthenticationConstants.ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER)).thenReturn(
                AuthenticationConstants.INTERNAL_APP_PRIVILEGE);

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertTrue(actual);
        verifyNoInteractions(response);
    }

    @Test
    void preHandleReturnsFalseIfMethodNotGetAndHasInternalPrivilegesButIdentityTypeIsOAUTH2() {
        // given
        when(request.getMethod()).thenReturn(HttpMethod.PATCH.name());
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY)).thenReturn(USER);
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY_TYPE)).thenReturn(
                AuthenticationConstants.OAUTH2_IDENTITY_TYPE);

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void preHandleReturnsFalseIfMethodNotGetAndNoInternalPrivileges() {
        // given
        when(request.getMethod()).thenReturn(HttpMethod.PATCH.name());
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY)).thenReturn(USER);
        when(request.getHeader(AuthenticationConstants.ERIC_IDENTITY_TYPE)).thenReturn(
                AuthenticationConstants.API_KEY_IDENTITY_TYPE);

        // when
        boolean actual = authenticationInterceptor.preHandle(request, response, handler);

        // then
        assertFalse(actual);
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
    }
}
