package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.sso.utils.AuthenticationException;
import org.ovirt.engine.core.sso.utils.AuthenticationUtils;
import org.ovirt.engine.core.sso.utils.Credentials;
import org.ovirt.engine.core.sso.utils.SSOConstants;
import org.ovirt.engine.core.sso.utils.SSOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractiveAuthServlet extends HttpServlet {
    private static final long serialVersionUID = -88168919566901736L;
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String PROFILE = "profile";

    private static Logger log = LoggerFactory.getLogger(InteractiveAuthServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("Entered InteractiveAuthServlet");
        try {
            String redirectUrl;
            if (StringUtils.isEmpty(SSOUtils.getSsoSession(request).getClientId())) {
                redirectUrl = SSOUtils.getSsoContext(request).getEngineUrl();
            } else {
                Credentials userCredentials = getUserCredentials(request);
                try {
                    if (SSOUtils.isUserAuthenticated(request)) {
                        log.debug("User is authenticated redirecting to {}", SSOConstants.INTERACTIVE_REDIRECT_TO_MODULE_URI);
                        redirectUrl = request.getContextPath() + SSOConstants.INTERACTIVE_REDIRECT_TO_MODULE_URI;
                    } else {
                        redirectUrl = authenticateUser(request, response, userCredentials);
                    }
                } catch (AuthenticationException ex) {
                    if (userCredentials != null) {
                        String profile = userCredentials.getProfile() == null ? "N/A" : userCredentials.getProfile();
                        log.error("Cannot authenticate user '{}@{}': {}", userCredentials.getUsername(), profile, ex.getMessage());
                        log.debug("Cannot authenticate user '{}@{}'", userCredentials.getUsername(), profile, ex);
                        SSOUtils.getSsoSession(request).setLoginMessage(ex.getMessage());
                    }
                    log.debug("Redirecting to LoginPage");
                    SSOUtils.getSsoSession(request).setReauthenticate(false);
                    redirectUrl = request.getContextPath() + SSOConstants.INTERACTIVE_LOGIN_FORM_URI;
                }
            }
            if (redirectUrl != null) {
                response.sendRedirect(redirectUrl);
            }
        } catch (Exception ex) {
            SSOUtils.redirectToErrorPage(request, response, ex);
        }
    }

    private String authenticateUser(HttpServletRequest request, HttpServletResponse response, Credentials userCredentials)
            throws ServletException, IOException, AuthenticationException {
        if (userCredentials == null || !userCredentials.isValid()) {
            throw new AuthenticationException("Invalid credentials");
        }
        try {
            log.debug("Authenticating user using credentials");
            response.addCookie(new Cookie("profile", userCredentials.getProfile()));
            AuthenticationUtils.handleCredentials(
                    request,
                    userCredentials);
            return request.getContextPath() + SSOConstants.INTERACTIVE_REDIRECT_TO_MODULE_URI;
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Internal Server Error: {}", ex.getMessage());
            log.debug("Internal Server Error", ex);
            throw new RuntimeException(ex);
        }
    }

    private Credentials getUserCredentials(HttpServletRequest request) throws Exception {
        String username = SSOUtils.getParameter(request, USERNAME);
        String password = SSOUtils.getParameter(request, PASSWORD);
        String profile = SSOUtils.getParameter(request, PROFILE);
        Credentials credentials;
        if (StringUtils.isEmpty(username) || password == null || StringUtils.isEmpty(profile)) {
            credentials = SSOUtils.getSsoSession(request).getTempCredentials();
        } else {
            credentials = new Credentials(username, password, profile, SSOUtils.getSsoContext(request).getSsoProfiles().contains(profile));
        }
        return credentials;
    }
}
