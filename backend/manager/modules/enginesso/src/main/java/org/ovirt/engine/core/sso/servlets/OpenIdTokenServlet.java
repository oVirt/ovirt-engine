package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.sso.api.AuthenticationException;
import org.ovirt.engine.core.sso.api.Credentials;
import org.ovirt.engine.core.sso.api.OAuthException;
import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.api.SsoSession;
import org.ovirt.engine.core.sso.service.OpenIdService;
import org.ovirt.engine.core.sso.service.SsoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenIdTokenServlet extends OAuthTokenServlet {

    private static Logger log = LoggerFactory.getLogger(OpenIdTokenServlet.class);

    @Inject
    private Instance<OpenIdService> openIdService;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            log.debug("Entered OpenIdTokenServlet Query String: {}, Parameters : {}",
                    request.getQueryString(),
                    SsoService.getRequestParameters(request));
            handleRequest(request, response);
        } catch (OAuthException ex) {
            SsoService.sendJsonDataWithMessage(request, response, ex);
        } catch (AuthenticationException ex) {
            SsoService.sendJsonDataWithMessage(request, response, SsoConstants.ERR_CODE_ACCESS_DENIED, ex);
        } catch (Exception ex) {
            SsoService.sendJsonDataWithMessage(request, response, SsoConstants.ERR_CODE_SERVER_ERROR, ex);
        }

    }

    @Override
    protected void issueTokenForAuthCode(
            HttpServletRequest request,
            HttpServletResponse response,
            String scope) throws Exception {
        String[] clientIdAndSecret = SsoService.getClientIdClientSecret(request);
        SsoService.validateClientRequest(request,
                clientIdAndSecret[0],
                clientIdAndSecret[1],
                scope,
                null);
        SsoSession ssoSession = handleIssueTokenForAuthCode(request, clientIdAndSecret[0], scope);
        log.debug("Sending json response");
        SsoService.sendJsonData(response, buildResponse(request, ssoSession, clientIdAndSecret[0], clientIdAndSecret[1]));
    }

    @Override
    protected String getTokenForAuthCode(String authCode) {
        return ssoContext.getTokenForOpenIdAuthCode(authCode);
    }

    protected void issueTokenForPasswd(
            HttpServletRequest request,
            HttpServletResponse response,
            String scope) throws Exception {
        log.debug("Entered issueTokenForPasswd");
        Credentials credentials = null;
        try {
            String[] clientIdAndSecret = SsoService.getClientIdClientSecret(request);
            SsoService.validateClientRequest(request,
                    clientIdAndSecret[0],
                    clientIdAndSecret[1],
                    scope,
                    null);
            String clientId = clientIdAndSecret[0];
            String clientSecret = clientIdAndSecret[1];

            credentials = getCredentials(request);
            SsoSession ssoSession = handleIssueTokenForPasswd(request, scope, credentials);
            log.debug("Sending json response");
            SsoService.sendJsonData(response, buildResponse(request, ssoSession, clientId, clientSecret));
        } catch (AuthenticationException ex) {
            String profile = "N/A";
            if (credentials != null) {
                profile = credentials.getProfile() == null ? "N/A" : credentials.getProfile();
            }
            throw new AuthenticationException(String.format(
                    ssoContext.getLocalizationUtils()
                            .localize(
                                    SsoConstants.APP_ERROR_CANNOT_AUTHENTICATE_USER_IN_DOMAIN,
                                    (Locale) request.getAttribute(SsoConstants.LOCALE)),
                    credentials == null ? "N/A" : credentials.getUsername(),
                    profile,
                    ex.getMessage()));
        }
    }

    protected Map<String, Object> buildResponse(HttpServletRequest request,
            SsoSession ssoSession,
            String clientId,
            String clientSecret) {
        Map<String, Object> payload = buildResponse(ssoSession);
        payload.put("id_token", openIdService.get().createJWT(request, ssoSession, clientId, clientSecret));
        return payload;
    }

    @Override
    protected void validateClientAcceptHeader(SsoSession ssoSession, HttpServletRequest request) {
        // empty
    }
}
