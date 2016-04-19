package org.ovirt.engine.core;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthCallbackServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(OAuthCallbackServlet.class);
    private static final long serialVersionUID = 5943389529927921616L;
    private static String moduleScope = "ovirt-app-admin ovirt-app-portal";

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
            ServletException {
        log.debug("Entered OAuthCallbackServlet");
        String authCode = request.getParameter(WelcomeUtils.CODE);
        String state = request.getParameter(WelcomeUtils.STATE);
        String stateInSession = (String) request.getSession(true).getAttribute(WelcomeUtils.STATE);
        String engineUri = EngineLocalConfig.getInstance().getProperty(WelcomeUtils.ENGINE_URI) + "/";
        String redirectUri = engineUri + "oauth2-callback";
        String token = "";
        if (StringUtils.isNotEmpty(stateInSession) && stateInSession.equals(state)) {
            if (StringUtils.isNotEmpty(authCode)) {
                String tokenForAuthCode = getTokenForAuthCode(request, authCode, moduleScope, redirectUri);
                if (StringUtils.isNotEmpty(tokenForAuthCode)) {
                    token = tokenForAuthCode;
                } else {
                    authCode = null;
                }
            }
        }

        request.getSession(true).setAttribute(WelcomeUtils.AUTH_CODE, authCode == null ? "" : authCode);
        if (StringUtils.isEmpty((String) request.getSession(true).getAttribute(WelcomeUtils.TOKEN))) {
            request.getSession(true).setAttribute(WelcomeUtils.TOKEN, token);
        }
        if (StringUtils.isNotEmpty(request.getParameter(WelcomeUtils.ERROR_CODE)) &&
                !WelcomeUtils.ERR_OVIRT_CODE_NOT_AUTHENTICATED.equals(request.getParameter(WelcomeUtils.ERROR_CODE))) {
            request.getSession(true).setAttribute(WelcomeUtils.ERROR, request.getParameter(WelcomeUtils.ERROR));
            request.getSession(true).setAttribute(WelcomeUtils.ERROR_CODE, request.getParameter(WelcomeUtils.ERROR_CODE));
        }
        log.debug("Redirecting to {}", engineUri);
        response.sendRedirect(engineUri);
        log.debug("Exited OAuthCallbackServlet");
    }

    private String getTokenForAuthCode(HttpServletRequest request, String authCode, String scope, String redirectUri) {
        String token  = null;
        Map<String, Object> tokenMap = SsoOAuthServiceUtils.getToken(WelcomeUtils.AUTHORIZATION_CODE, authCode, scope, redirectUri);
        if (tokenMap.containsKey(WelcomeUtils.ERROR)) {
            request.getSession(true).setAttribute(WelcomeUtils.ERROR, tokenMap.get(WelcomeUtils.ERROR));
            request.getSession(true).setAttribute(WelcomeUtils.ERROR_CODE, tokenMap.get(WelcomeUtils.ERROR_CODE));
        } else {
            token = (String) tokenMap.get(WelcomeUtils.JSON_ACCESS_TOKEN);
        }
        return token;
    }
}
