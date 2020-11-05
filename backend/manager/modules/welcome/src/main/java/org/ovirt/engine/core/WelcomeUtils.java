package org.ovirt.engine.core;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.utils.EngineLocalConfig;

public class WelcomeUtils {

    public static final String AUTH_CODE = "authCode";
    public static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String CAPABILITY_CREDENTIALS_CHANGE = "capability_credentials_change";
    public static final String CODE = "code";
    public static final String ENGINE_URI = "ENGINE_URI";
    public static final String ENGINE_SSO_CLIENT_ID = "ENGINE_SSO_CLIENT_ID";
    public static final String ENGINE_SSO_ENABLE_EXTERNAL_SSO = "engine_sso_enable_external_sso";
    public static final String LOCALE = "locale";
    public static final String REAUTHENTICATE = "reauthenticate";
    public static final String SCOPE = "scope";
    public static final String SSO_USER = "sso_user";
    public static final String TOKEN = "token";

    public static final String CREDENTIALS_CHANGE_FORM_URI = "/credentials-change.html";
    public static final String LOGIN_URI = "/login";
    public static final String LOGOUT_URI = "/logout";
    public static final String OAUTH_CALLBACK_URL_FORMAT = "%s://%s:%s%s/oauth2-callback";
    public static final String OAUTH_AUTHORIZE_URI = "/oauth/authorize";
    public static final String SWITCH_USER_URI = "/switch-user";
    public static final String WELCOME_PAGE_JSP_URI = "/WEB-INF/ovirt-engine.jsp";

    public static final String HTTP_PARAM_LOCALE = "locale";
    public static final String HTTP_PARAM_REDIRECT_URI = "redirect_uri";
    public static final String HTTP_PARAM_SCOPE = "scope";
    public static final String HTTP_PARAM_CLIENT_ID = "client_id";
    public static final String HTTP_PARAM_RESPONSE_TYPE = "response_type";
    public static final String HTTP_PARAM_SOURCE_ADDR = "source_addr";

    public static final String JSON_ACCESS_TOKEN = "access_token";
    public static final String JSON_USER_ID = "user_id";
    public static final String JSON_USER_AUTHZ = "user_authz";

    public static final String ERROR = "error";
    public static final String ERROR_DESCRIPTION = "error_description";
    public static final String ERR_CODE_INVALID_GRANT = "invalid_grant";
    public static final String ERR_OVIRT_CODE_NOT_AUTHENTICATED = "not_authenticated";

    public static String getOauth2CallbackUrl(HttpServletRequest request) {
        return String.format(OAUTH_CALLBACK_URL_FORMAT, request.getScheme(),
                FiltersHelper.getRedirectUriServerName(request.getServerName()),
                request.getServerPort(),
                EngineLocalConfig.getInstance().getProperty(WelcomeUtils.ENGINE_URI));
    }

    public static String getLoginUrl(String engineUri, String scope) {
        return String.format("%s%s?%s=%s",
                engineUri,
                WelcomeUtils.LOGIN_URI,
                WelcomeUtils.SCOPE,
                URLEncoder.encode(scope, StandardCharsets.UTF_8));
    }
}
