package uk.gov.companieshouse.filinghistory.api.interceptor;

public final class AuthenticationConstants {

    private AuthenticationConstants() {
    }

    public static final String OAUTH2_IDENTITY_TYPE = "oauth2";
    public static final String API_KEY_IDENTITY_TYPE = "key";
    public static final String ERIC_AUTHORISED_KEY_PRIVILEGES_HEADER = "ERIC-Authorised-Key-Privileges";
    public static final String ERIC_IDENTITY = "ERIC-Identity";
    public static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    public static final String INTERNAL_APP_PRIVILEGE = "internal-app";

}
