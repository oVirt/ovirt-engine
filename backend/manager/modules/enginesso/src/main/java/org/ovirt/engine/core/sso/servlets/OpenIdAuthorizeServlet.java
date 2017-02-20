package org.ovirt.engine.core.sso.servlets;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.sso.utils.InteractiveAuth;
import org.ovirt.engine.core.sso.utils.SsoConstants;
import org.ovirt.engine.core.sso.utils.SsoSession;
import org.ovirt.engine.core.sso.utils.SsoUtils;

public class OpenIdAuthorizeServlet extends OAuthAuthorizeServlet {

    private static List<String> unsupportedScopes = Arrays.asList("profile", "email", "address", "phone");

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
            long maxAge = Long.parseLong(maxAgeStr);
            if (Duration.between(ssoSession.getAuthTime().toInstant(), Instant.now()).toMillis() > maxAge) {
                ssoSession.setStatus(SsoSession.Status.unauthenticated);
            }
        }
        return ssoSession;
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
