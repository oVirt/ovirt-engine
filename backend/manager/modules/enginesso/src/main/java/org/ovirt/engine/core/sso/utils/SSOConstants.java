package org.ovirt.engine.core.sso.utils;

public class SSOConstants {
    public static final String OVIRT_SSO_VERSION = "0";
    public static final String OVIRT_SSO_CONTEXT = "ovirt-ssoContext";
    public static final String OVIRT_SSO_SESSION = "ovirt-ssoSession";

    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final String HTTP_PARAM_AUTHORIZATION_CODE = "code";
    public static final String HTTP_PARAM_AUTH_RECORD = "ovirt_auth_record";
    public static final String HTTP_PARAM_TOKEN = "token";
    public static final String HTTP_PARAM_REDIRECT_URI = "redirect_uri";
    public static final String HTTP_PARAM_SCOPE = "scope";
    public static final String HTTP_PARAM_STATE = "state";
    public static final String HTTP_PARAM_CLIENT_ID = "client_id";
    public static final String HTTP_PARAM_CLIENT_SECRET = "client_secret";
    public static final String HTTP_REQ_ATTR_ACCESS_TOKEN = "access_token";

    public static final String INTERACTIVE_LOGIN_NEGOTIATE_URI = "/interactive-login-negotiate/ovirt-auth";
    public static final String INTERACTIVE_LOGIN_BASIC_URI = "/interactive-login-basic";
    public static final String INTERACTIVE_LOGIN_BASIC_ENFORCE_URI = "/interactive-login-basic-enforce";
    public static final String INTERACTIVE_LOGIN_URI = "/interactive-login";
    public static final String INTERACTIVE_LOGIN_NEXT_AUTH_URI = "/interactive-login-next-auth";
    public static final String INTERACTIVE_REDIRECT_TO_MODULE_URI = "/interactive-redirect-to-module";
    public static final String INTERACTIVE_LOGIN_FORM_URI = "/login.html";

    public static final String JSON_ACCESS_TOKEN = "access_token";
    public static final String JSON_ACTIVE = "active";
    public static final String JSON_CLIENT_ID = "client_id";
    public static final String JSON_EXPIRES_IN = "exp";
    public static final String JSON_GRANT_TYPE = "grant_type";
    public static final String JSON_RESPONSE_TYPE = "response_type";
    public static final String JSON_SCOPE = "scope";
    public static final String JSON_TOKEN_TYPE = "token_type";
    public static final String JSON_USER_ID = "user_id";

    public static final String ERROR_CODE = "error_code";
    public static final String ERROR = "error";
    public static final String ERR_CODE_INVALID_GRANT = "invalid_grant";
    public static final String ERR_CODE_UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";
    public static final String ERR_CODE_INVALID_REQUEST = "invalid_request";
    public static final String ERR_OVIRT_CODE_NOT_AUTHENTICATED = "not_authenticated";
    public static final String ERR_CODE_UNAUTHORIZED_CLIENT = "unauthorized_client";
    public static final String ERR_CODE_ACCESS_DENIED = "access_denied";
    public static final String ERR_CODE_INVALID_SCOPE = "invalid_scope";
    public static final String ERR_CODE_SERVER_ERROR = "server_error";
    public static final String ERR_CODE_UNSUPPORTED_GRANT_TYPE_MSG = "The authorization grant type is not supported by the authorization server.";
    public static final String ERR_CODE_INVALID_REQUEST_MSG = "Invalid request, parameter '%s' not found or contains invalid value.";
    public static final String ERR_CODE_NOT_AUTHENTICATED_MSG = "The user is not authenticated.";
    public static final String ERR_CODE_UNAUTHORIZED_CLIENT_MSG = "The client is not authorized to request an authorization.";
    public static final String ERR_CODE_ACCESS_DENIED_MSG = "The resource owner or authorization server denied the request.";
    public static final String ERR_CODE_INVALID_SCOPE_MSG = "The requested scope '%s' is invalid, unknown, malformed, or exceeds the scope granted by the resource owner.";
}
