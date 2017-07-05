package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.sso.utils.InteractiveAuth;
import org.ovirt.engine.core.sso.utils.OAuthException;
import org.ovirt.engine.core.sso.utils.SsoConstants;
import org.ovirt.engine.core.sso.utils.SsoSession;
import org.ovirt.engine.core.sso.utils.SsoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenIdAuthorizeServlet extends OAuthAuthorizeServlet {

    private static Logger log = LoggerFactory.getLogger(OpenIdAuthorizeServlet.class);
    private static List<String> unsupportedScopes = Arrays.asList("profile", "email", "address", "phone");

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            handleRequest(request, response);
        } catch (Exception ex) {
            SsoSession ssoSession = SsoUtils.getSsoSession(request, true);
            ssoSession.setRedirectUri(request.getParameter(SsoConstants.HTTP_PARAM_REDIRECT_URI));
            if (ex instanceof OAuthException &&
                    ((OAuthException) ex).getCode().equals(SsoConstants.ERR_CODE_INVALID_REQUEST) &&
                    ex.getMessage().equals(SsoConstants.ERR_REDIRECT_URI_NOTREG_MSG)) {
                SsoUtils.sendJsonDataWithMessage(request, response, (OAuthException) ex);
            } else {
                SsoUtils.redirectToErrorPage(request, response, ex);
            }
        }
    }

    @Override
    protected void validateClientRequest(HttpServletRequest request,
            String clientId,
            String scope,
            String redirectUri) {
        List<String> scopes = new ArrayList<>(SsoUtils.scopeAsList(scope));
        // remove all unsupported scopes
        scopes.removeAll(unsupportedScopes);
        SsoUtils.validateClientRequest(request, clientId, null, StringUtils.join(scopes, ' '), redirectUri);
    }

    protected SsoSession buildSsoSession(HttpServletRequest request)
            throws Exception {
        SsoSession ssoSession = super.buildSsoSession(request);
        ssoSession.setOpenIdScope(true);
        ssoSession.setOpenIdNonce(request.getParameter(SsoConstants.HTTP_PARAM_OPENID_NONCE));
        ssoSession.setOpenIdPrompt(request.getParameter(SsoConstants.HTTP_PARAM_OPENID_PROMPT));
        ssoSession.setOpenIdDisplay(request.getParameter(SsoConstants.HTTP_PARAM_OPENID_DISPLAY));
        String maxAgeStr = request.getParameter(SsoConstants.HTTP_PARAM_OPENID_MAX_AGE);
        if ("login".equals(ssoSession.getOpenIdPrompt())) {
            ssoSession.setStatus(SsoSession.Status.unauthenticated);
        } else
        if (ssoSession.getStatus() == SsoSession.Status.authenticated && StringUtils.isNotEmpty(maxAgeStr)) {
            long maxAge = Long.parseLong(maxAgeStr) * 1000;
            if (Duration.between(ssoSession.getAuthTime().toInstant(), Instant.now()).toMillis() > maxAge) {
                ssoSession.setStatus(SsoSession.Status.unauthenticated);
            }
        }
        return ssoSession;
    }

    @Override
    protected void login(
            HttpServletRequest request,
            HttpServletResponse response,
            SsoSession ssoSession) throws Exception {
        log.debug("Entered login queryString: {}", request.getQueryString());

        switch (ssoSession.getStatus()) {
            case unauthenticated:
                if (StringUtils.isNotEmpty(ssoSession.getAccessToken()) &&
                        StringUtils.isNotEmpty(ssoSession.getAuthorizationCode())) {
                    ssoSession = (SsoSession) ssoSession.clone();
                    request.getSession().setAttribute(SsoConstants.OVIRT_SSO_SESSION, ssoSession);
                }
                break;
            case authenticated:
                ssoSession.setTokenIssued(false);
                ssoSession.setActive(true);
                break;
        }

        super.login(request, response, ssoSession);
    }

    @Override
    protected Stack<InteractiveAuth> getAuthSeq(SsoSession ssoSession) {
        Stack<InteractiveAuth> authSeq = super.getAuthSeq(ssoSession);
        if ("none".equals(ssoSession.getOpenIdPrompt())) {
            authSeq.remove(InteractiveAuth.I);
            authSeq.remove(InteractiveAuth.B);
        }
        if ("popup".equals(ssoSession.getOpenIdDisplay())) {
            authSeq.remove(InteractiveAuth.I);
            authSeq.push(InteractiveAuth.b);
            authSeq.push(InteractiveAuth.B);
        }
        return authSeq;
    }
}
