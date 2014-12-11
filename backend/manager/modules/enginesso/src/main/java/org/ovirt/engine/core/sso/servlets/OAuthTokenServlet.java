package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.core.sso.utils.AuthResult;
import org.ovirt.engine.core.sso.utils.AuthenticationException;
import org.ovirt.engine.core.sso.utils.AuthenticationUtils;
import org.ovirt.engine.core.sso.utils.Credentials;
import org.ovirt.engine.core.sso.utils.NegotiateAuthUtils;
import org.ovirt.engine.core.sso.utils.NonInteractiveAuth;
import org.ovirt.engine.core.sso.utils.OAuthException;
import org.ovirt.engine.core.sso.utils.SSOConstants;
import org.ovirt.engine.core.sso.utils.SSOSession;
import org.ovirt.engine.core.sso.utils.SSOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthTokenServlet extends HttpServlet {
    private static final long serialVersionUID = 7168485079055058668L;
    private static Logger log = LoggerFactory.getLogger(OAuthTokenServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            log.debug("Entered OAuthTokenServlet Query String: {}, Parameters : {}",
                    request.getQueryString(),
                    SSOUtils.getRequestParameters(request));
            String grantType = SSOUtils.getRequestParameter(request, SSOConstants.JSON_GRANT_TYPE, SSOConstants.JSON_GRANT_TYPE);
            String scope = SSOUtils.getScopeRequestParameter(request, "");
            SSOUtils.validateClientAcceptHeader(request);

            switch(grantType) {
                case "authorization_code":
                    String[] clientIdAndSecret = SSOUtils.getClientIdClientSecret(request);
                    SSOUtils.validateClientRequest(request, clientIdAndSecret[0], clientIdAndSecret[1], scope, null);
                    issueTokenForAuthCode(request, response, clientIdAndSecret[0], scope);
                    break;
                case "password":
                    handlePasswordGrantType(request, response, scope);
                    break;
                case "urn:ovirt:params:oauth:grant-type:http":
                    issueTokenUsingHttpHeaders(request, response);
                    break;
                default:
                    throw new OAuthException(SSOConstants.ERR_CODE_UNSUPPORTED_GRANT_TYPE, SSOConstants.ERR_CODE_UNSUPPORTED_GRANT_TYPE_MSG);
            }
        } catch(OAuthException ex) {
            SSOUtils.sendJsonDataWithMessage(response, ex);
        } catch(AuthenticationException ex) {
            SSOUtils.sendJsonDataWithMessage(response, SSOConstants.ERR_CODE_ACCESS_DENIED, ex);
        } catch(Exception ex) {
            SSOUtils.sendJsonDataWithMessage(response, SSOConstants.ERR_CODE_SERVER_ERROR, ex);
        }

    }

    private static void issueTokenForAuthCode(HttpServletRequest request, HttpServletResponse response, String clientId, String scope) throws Exception {
        log.debug("Entered issueTokenForAuthCode");
        String authCode = SSOUtils.getRequestParameter(request, SSOConstants.HTTP_PARAM_AUTHORIZATION_CODE, SSOConstants.HTTP_PARAM_AUTHORIZATION_CODE);
        String accessToken = SSOUtils.getSsoContext(request).getTokenForAuthCode(authCode);
        SSOUtils.validateRequestScope(request, accessToken, scope);
        SSOSession ssoSession = SSOUtils.getSsoSession(request, clientId, accessToken, true);
        log.debug("Sending json response");
        SSOUtils.sendJsonData(response, buildResponse(ssoSession));
    }

    private void handlePasswordGrantType(HttpServletRequest request, HttpServletResponse response, String scope) throws Exception {
        if (SSOUtils.scopeAsList(scope).contains("ovirt-ext=token:login-on-behalf")) {
            issueTokenForLoginOnBehalf(request, response, scope);
        } else {
            issueTokenForPasswd(request, response, scope);
        }
    }

    private static void issueTokenForLoginOnBehalf(HttpServletRequest request, HttpServletResponse response, String scope) throws Exception {
        log.debug("Entered issueTokenForLoginOnBehalf");
        String[] clientIdAndSecret = SSOUtils.getClientIdClientSecret(request);
        String username = SSOUtils.getRequestParameter(request, "username", null);
        log.debug("Attempting to issueTokenForLoginOnBehalf for client: {}, user: {}", clientIdAndSecret[0], username);
        AuthenticationUtils.loginOnBehalf(request, username);
        String token = (String) request.getAttribute(SSOConstants.HTTP_REQ_ATTR_ACCESS_TOKEN);
        SSOUtils.validateRequestScope(request, token, scope);
        SSOSession ssoSession = SSOUtils.getSsoSession(request, token, true);
        if (ssoSession == null) {
            throw new OAuthException(SSOConstants.ERR_CODE_INVALID_GRANT, "The provided authorization grant for the username and password has expired");
        }
        log.debug("Sending json response");
        SSOUtils.sendJsonData(response, buildResponse(ssoSession));
    }

    private static void issueTokenForPasswd(HttpServletRequest request, HttpServletResponse response, String scope) throws Exception {
        log.debug("Entered issueTokenForPasswd");
        Credentials credentials = null;
        try {
            credentials = SSOUtils.translateUser(SSOUtils.getRequestParameter(request, "username"),
                    SSOUtils.getRequestParameter(request, "password"),
                    SSOUtils.getSsoContext(request));
            String token = null;
            if (credentials != null && credentials.isValid()) {
                AuthenticationUtils.handleCredentials(request, credentials);
                token = (String) request.getAttribute(SSOConstants.HTTP_REQ_ATTR_ACCESS_TOKEN);
            }
            log.debug("Attempting to issueTokenForPasswd for user: {}", credentials.getUsername());
            SSOSession ssoSession = SSOUtils.getSsoSessionFromRequest(request, token);
            if (ssoSession == null) {
                throw new OAuthException(SSOConstants.ERR_CODE_INVALID_GRANT, "The provided authorization grant for the username and password has expired");
            }
            SSOUtils.validateRequestScope(request, token, scope);
            log.debug("Sending json response");
            SSOUtils.sendJsonData(response, buildResponse(ssoSession));
        } catch (AuthenticationException ex) {
            String profile = "N/A";
            if (credentials != null) {
                profile = credentials.getProfile() == null ? "N/A" : credentials.getProfile();
            }
            throw new AuthenticationException(String.format("Cannot authenticate user '%s@%s': %s",
                    credentials == null ? "N/A" : credentials.getUsername(),
                    profile,
                    ex.getMessage()));
        }
    }

    private static void issueTokenUsingHttpHeaders(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("Entered issueTokenUsingHttpHeaders");
        try {
            AuthResult authResult = null;
            for (NonInteractiveAuth auth : getAuthSeq(request)) {
                authResult = auth.doAuth(request, response);
                if (authResult.getStatus() == Authn.AuthResult.SUCCESS ||
                        authResult.getStatus() == Authn.AuthResult.NEGOTIATION_INCOMPLETE) {
                    break;
                }
            }
            if (authResult != null && authResult.getStatus() != Authn.AuthResult.SUCCESS) {
                log.debug("Authentication failed using http headers");
                List<String> schemes = (List<String>) request.getAttribute(NegotiateAuthUtils.REQUEST_SCHEMES_KEY);
                for (String scheme : new HashSet<>(schemes == null ? Collections.<String>emptyList() : schemes)) {
                    response.setHeader("WWW-Authenticate", scheme);
                }
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else if (authResult != null && StringUtils.isNotEmpty(authResult.getToken())) {
                SSOSession ssoSession = SSOUtils.getSsoSessionFromRequest(request, authResult.getToken());
                if (ssoSession == null) {
                    throw new OAuthException(SSOConstants.ERR_CODE_INVALID_GRANT, "The provided authorization grant has expired");
                }
                log.debug("Sending json response");
                SSOUtils.sendJsonData(response, buildResponse(ssoSession));
            } else {
                throw new AuthenticationException("Authentication failed.");
            }
        } catch (Exception ex) {
            throw new AuthenticationException(String.format("Cannot authenticate user : %s", ex.getMessage()));
        }
    }

    private static Map<String, Object> buildResponse(SSOSession ssoSession) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(SSOConstants.JSON_ACCESS_TOKEN, ssoSession.getAccessToken());
        payload.put(SSOConstants.JSON_SCOPE, StringUtils.isEmpty(ssoSession.getScope()) ? "" : ssoSession.getScope());
        payload.put(SSOConstants.JSON_EXPIRES_IN, ssoSession.getValidTo());
        payload.put(SSOConstants.JSON_TOKEN_TYPE, "bearer");
        return payload;
    }

    private static List<NonInteractiveAuth> getAuthSeq(HttpServletRequest request) {
        String appAuthSeq = SSOUtils.getSsoContext(request).getSsoLocalConfig().getProperty("SSO_TOKEN_HTTP_LOGIN_SEQUENCE");
        List<NonInteractiveAuth> authSeqList = new ArrayList<>();
        if (StringUtils.isNotEmpty(appAuthSeq)) {
            for (char c : appAuthSeq.toCharArray()) {
                if (c == '~') {
                    continue;
                }
                try {
                    authSeqList.add(Enum.valueOf(NonInteractiveAuth.class, "" + c));
                } catch (IllegalArgumentException e) {
                    log.error("Unable to retrieve auth for value {}: {}", c, e.getMessage());
                    log.debug("Unable to retrieve auth", e);
                }
            }
        }
        return authSeqList;
    }
}
