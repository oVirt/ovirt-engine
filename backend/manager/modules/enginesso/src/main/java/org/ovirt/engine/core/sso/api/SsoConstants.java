package org.ovirt.engine.core.sso.api;

public class SsoConstants {

    public static final String LOCALE = "locale";

    public static final String APP_MESSAGE_FILENAME = "AppMessages.properties";
    public static final String APP_ERROR_AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
    public static final String APP_ERROR_AUTHENTICATION_REQUIRED = "AUTHENTICATION_REQUIRED";
    public static final String APP_ERROR_AUTHORIZATION_GRANT_EXPIRED_FOR_USERNAME_PASSWORD =
            "AUTHORIZATION_GRANT_EXPIRED_FOR_USERNAME_PASSWORD";
    public static final String APP_ERROR_AUTHORIZATION_GRANT_EXPIRED = "AUTHORIZATION_GRANT_EXPIRED";
    public static final String APP_ERROR_CANNOT_AUTHENTICATE_USER = "CANNOT_AUTHENTICATE_USER";
    public static final String APP_ERROR_CANNOT_AUTHENTICATE_USER_IN_DOMAIN = "CANNOT_AUTHENTICATE_USER_IN_DOMAIN";
    public static final String APP_ERROR_CHANGE_PASSWORD_FAILED = "CHANGE_PASSWORD_FAILED";
    public static final String APP_ERROR_INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String APP_ERROR_INVALID_GRANT = "INVALID_GRANT";
    public static final String APP_ERROR_NO_VALID_AUTHENTICATION_MECHANISM_FOUND =
            "NO_VALID_AUTHENTICATION_MECHANISM_FOUND";
    public static final String APP_ERROR_NO_PROFILE_IN_CREDENTIALS = "NO_PROFILE_IN_CREDENTIALS";
    public static final String APP_ERROR_NO_USER_NAME_IN_CREDENTIALS = "NO_USER_NAME_IN_CREDENTIALS";
    public static final String APP_ERROR_NO_USER_NAME_IN_CREDENTIALS_INTERACTIVE_AUTH =
            "NO_USER_NAME_IN_CREDENTIALS_INTERACTIVE_AUTH";
    public static final String APP_ERROR_NO_VALID_PROFILE_IN_CREDENTIALS = "NO_VALID_PROFILE_IN_CREDENTIALS";
    public static final String APP_ERROR_PASSWORDS_DONT_MATCH = "PASSWORDS_DONT_MATCH";
    public static final String APP_ERROR_PROVIDE_USERNAME_PASSWORD_AND_PROFILE =
            "PROVIDE_USERNAME_PASSWORD_AND_PROFILE";
    public static final String APP_ERROR_PROVIDE_USERNAME_AND_PROFILE = "PROVIDE_USERNAME_AND_PROFILE";
    public static final String APP_ERROR_REDIRECT_URI_NOTREG_MSG = "REDIRECT_URI_NOTREG_MSG";
    public static final String APP_ERROR_NOT_VALID_FQDN_MSG = "NOT_VALID_FQDN_MSG";
    public static final String APP_ERROR_SESSION_EXPIRED = "SESSION_EXPIRED";
    public static final String APP_ERROR_UNABLE_TO_EXTRACT_CREDENTIALS = "UNABLE_TO_EXTRACT_CREDENTIALS";
    public static final String APP_ERROR_UNABLE_TO_DECODE_SESSION_ID_TOKEN = "UNABLE_TO_DECODE_SESSION_ID_TOKEN";
    public static final String APP_ERROR_UNSUPPORTED_PARAMETER_IN_REQUEST = "UNSUPPORTED_PARAMETER_IN_REQUEST";
    public static final String APP_ERROR_USER_PASSWORD_EXPIRED_CHANGE_URL_PROVIDED =
            "USER_PASSWORD_EXPIRED_CHANGE_URL_PROVIDED";
    public static final String APP_ERROR_USER_FAILED_TO_AUTHENTICATE = "USER_FAILED_TO_AUTHENTICATE";
    public static final String APP_ERROR_USER_ACCOUNT_DISABLED = "USER_ACCOUNT_DISABLED";
    public static final String APP_ERROR_USER_ACCOUNT_EXPIRED = "USER_ACCOUNT_EXPIRED";
    public static final String APP_ERROR_USER_PASSWORD_EXPIRED = "USER_PASSWORD_EXPIRED";
    public static final String APP_ERROR_USER_FAILED_TO_AUTHENTICATE_TIMED_OUT =
            "USER_FAILED_TO_AUTHENTICATE_TIMED_OUT";
    public static final String APP_MSG_CHANGE_PASSWORD_SUCCEEDED = "CHANGE_PASSWORD_SUCCEEDED";

    public static final String OVIRT_SSO_VERSION = "0";
    public static final String OVIRT_SSO_CONTEXT = "ovirt-ssoContext";
    public static final String OVIRT_SSO_SESSION = "ovirt-ssoSession";

    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final String HTTP_PARAM_AUTHORIZATION_CODE = "code";
    public static final String HTTP_PARAM_AUTH_RECORD = "ovirt_auth_record";
    public static final String HTTP_PARAM_DOMAIN = "domain";
    public static final String HTTP_PARAM_GROUPS_RESOLVING = "groups_resolving";
    public static final String HTTP_PARAM_GROUPS_RESOLVING_RECURSIVE = "groups_resolving_recursive";
    public static final String HTTP_PARAM_PARAMS = "params";
    public static final String HTTP_PARAM_PRINCIPAL = "principal";
    public static final String HTTP_PARAM_ID = "id";
    public static final String HTTP_PARAM_IDS = "ids";
    public static final String HTTP_PARAM_NAMESPACE = "namespace";
    public static final String HTTP_PARAM_OPENID_DISPLAY = "display";
    public static final String HTTP_PARAM_OPENID_NONCE = "nonce";
    public static final String HTTP_PARAM_OPENID_PROMPT = "prompt";
    public static final String HTTP_PARAM_OPENID_MAX_AGE = "max_age";
    public static final String HTTP_PARAM_TOKEN = "token";
    public static final String HTTP_PARAM_TOKENS = "tokens";
    public static final String HTTP_PARAM_APP_URL = "app_url";
    public static final String HTTP_PARAM_REDIRECT_URI = "redirect_uri";
    public static final String HTTP_PARAM_SCOPE = "scope";
    public static final String HTTP_PARAM_STATE = "state";
    public static final String HTTP_PARAM_CLIENT_ID = "client_id";
    public static final String HTTP_PARAM_SOURCE_ADDR = "source_addr";
    public static final String HTTP_PARAM_CLIENT_SECRET = "client_secret";
    public static final String HTTP_REQ_ATTR_ACCESS_TOKEN = "access_token";
    public static final String HTTP_REQ_HEADER_OIDC_ACCESS_TOKEN = "OIDC_access_token";
    public static final String HTTP_PARAM_SEARCH_QUERY_TYPE = "query_type";

    public static final String INTERACTIVE_LOGIN_NEGOTIATE_URI = "/interactive-login-negotiate/ovirt-auth";
    public static final String INTERACTIVE_LOGIN_BASIC_URI = "/interactive-login-basic";
    public static final String INTERACTIVE_LOGIN_BASIC_ENFORCE_URI = "/interactive-login-basic-enforce";
    public static final String INTERACTIVE_LOGIN_URI = "/interactive-login";
    public static final String INTERACTIVE_LOGIN_NEXT_AUTH_URI = "/interactive-login-next-auth";
    public static final String INTERACTIVE_REDIRECT_TO_MODULE_URI = "/interactive-redirect-to-module";
    public static final String INTERACTIVE_LOGIN_FORM_URI = "/login.html";
    public static final String INTERACTIVE_CHANGE_PASSWD_FORM_URI = "/credentials-change.html";

    public static final String JSON_ACCESS_TOKEN = "access_token";
    public static final String JSON_ACTIVE = "active";
    public static final String JSON_CLIENT_ID = "client_id";
    public static final String JSON_EXPIRES_IN = "exp";
    public static final String JSON_GRANT_TYPE = "grant_type";
    public static final String JSON_RESPONSE_TYPE = "response_type";
    public static final String JSON_SCOPE = "scope";
    public static final String JSON_TOKEN_TYPE = "token_type";
    public static final String JSON_USER_ID = "user_id";
    public static final String JSON_USER_AUTHZ = "user_authz";

    public static final String ERROR = "error";
    public static final String ERROR_CODE = "error_code";
    public static final String ERROR_DESCRIPTION = "error_description";
    public static final String ERR_CODE_INVALID_GRANT = "invalid_grant";
    public static final String ERR_CODE_UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";
    public static final String ERR_CODE_BAD_REQUEST_EXTERNAL_AUTH_ENABLED_TYPE = "unsupported_request";
    public static final String ERR_CODE_INVALID_REQUEST = "invalid_request";
    public static final String ERR_CODE_INVALID_TOKEN = "invalid_token";
    public static final String ERR_OVIRT_CODE_NOT_AUTHENTICATED = "not_authenticated";
    public static final String ERR_CODE_OPENID_LOGIN_REQUIRED = "login_required";
    public static final String ERR_CODE_UNAUTHORIZED_CLIENT = "unauthorized_client";
    public static final String ERR_CODE_ACCESS_DENIED = "access_denied";
    public static final String ERR_CODE_INVALID_SCOPE = "invalid_scope";
    public static final String ERR_CODE_SERVER_ERROR = "server_error";
    public static final String ERR_CODE_UNSUPPORTED_GRANT_TYPE_MSG = "The authorization grant type is not supported " +
            "by the authorization server.";
    public static final String ERR_CODE_BAD_REQUEST_EXTERNAL_AUTH_ENABLED_MSG = "Issuing tokens is not supported " +
            "by the authorization server. External SSO authentication has been enabled, tokens need to be obtained " +
            "from the external server.";
    public static final String ERR_CODE_INVALID_CODE_MSG = "Code not valid";
    public static final String ERR_CODE_INVALID_REQUEST_MSG = "Missing parameter: '%s'";
    public static final String ERR_CODE_NOT_AUTHENTICATED_MSG = "The user is not authenticated.";
    public static final String ERR_CODE_UNAUTHORIZED_CLIENT_MSG = "The client is not authorized to request an " +
            "authorization. It's required to access the system using FQDN.";
    public static final String ERR_CODE_ACCESS_DENIED_MSG = "The resource owner or authorization server denied the " +
            "request.";
    public static final String ERR_CODE_INVALID_SCOPE_MSG = "The requested scope '%s' is invalid, unknown, " +
            "malformed, or exceeds the scope granted by the resource owner.";
    public static final String ERR_REDIRECT_URI_NOTREG_MSG = "The redirection URI for client is not registered";
    public static final String ERR_SESSION_EXPIRED_MSG = "Session expired";

    public static final String AVAILABLE_NAMESPACES_QUERY = "available-namespaces";
    public static final String DOMAIN_LIST_QUERY = "domain-list";
    public static final String FIND_DIRECTORY_GROUP_BY_ID_QUERY = "find-directory-group-by-id";
    public static final String FETCH_PRINCIPAL_RECORD_QUERY = "fetch-principal-record";
    public static final String FIND_PRINCIPAL_BY_ID_QUERY = "find-principal-by-id";
    public static final String FIND_PRINCIPALS_BY_IDS_QUERY = "find-principals-by-ids";
    public static final String FIND_LOGIN_ON_BEHALF_PRINCIPAL_BY_ID_QUERY = "find-login-on-behalf-principal-by-id";
    public static final String PROFILE_LIST_QUERY = "profile-list";
    public static final String SEARCH_GROUPS_QUERY = "groups";
    public static final String SEARCH_USERS_QUERY = "users";
    public static final String SESSION_STATUES_QUERY = "session-statuses";

    public static final String AUTHZ_SEARCH_SCOPE = "ovirt-ext=token-info:authz-search";
    public static final String OPENID_SCOPE = "openid";
    public static final String OVIRT_APP_API_SCOPE = "ovirt-app-api";
    public static final String OVIRT_APP_ADMIN_SCOPE = "ovirt-app-admin";
    public static final String OVIRT_APP_PORTAL_SCOPE = "ovirt-app-portal";
    public static final String PASSWORD_ACCESS_SCOPE = "ovirt-ext=token:password-access";
    public static final String PUBLIC_AUTHZ_SEARCH_SCOPE = "ovirt-ext=token-info:public-authz-search";
    public static final String VALIDATE_SCOPE = "ovirt-ext=token-info:validate";
}
