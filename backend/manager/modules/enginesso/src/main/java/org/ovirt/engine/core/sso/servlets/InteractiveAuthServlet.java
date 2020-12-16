package org.ovirt.engine.core.sso.servlets;

import java.util.Arrays;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.sso.api.AuthenticationException;
import org.ovirt.engine.core.sso.api.Credentials;
import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.api.SsoContext;
import org.ovirt.engine.core.sso.api.SsoSession;
import org.ovirt.engine.core.sso.service.AuthenticationService;
import org.ovirt.engine.core.sso.service.SsoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractiveAuthServlet extends HttpServlet {
    private static final long serialVersionUID = -88168919566901736L;
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String PROFILE = "profile";

    private static Logger log = LoggerFactory.getLogger(InteractiveAuthServlet.class);

    private SsoContext ssoContext;

    @Override
    public void init(ServletConfig config) {
        ssoContext = SsoService.getSsoContext(config.getServletContext());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        log.debug("Entered InteractiveAuthServlet");
        try {
            String redirectUrl;
            SsoSession ssoSession = SsoService.getSsoSession(request);
            // clean up the sso session id token
            ssoContext.removeSsoSessionById(ssoSession);
            if (StringUtils.isEmpty(ssoSession.getClientId())) {
                redirectUrl = ssoContext.getEngineUrl();
            } else {
                Credentials userCredentials = getUserCredentials(request);
                try {
                    if (SsoService.isUserAuthenticated(request)) {
                        log.debug("User is authenticated redirecting to {}",
                                SsoConstants.INTERACTIVE_REDIRECT_TO_MODULE_URI);
                        redirectUrl = request.getContextPath() + SsoConstants.INTERACTIVE_REDIRECT_TO_MODULE_URI;
                    } else {
                        redirectUrl = authenticateUser(request, response, userCredentials);
                    }
                } catch (AuthenticationException ex) {
                    if (userCredentials != null) {
                        String profile = userCredentials.getProfile() == null ? "N/A" : userCredentials.getProfile();
                        String authzName = ssoContext.getUserAuthzName(ssoSession);
                        String userDomainSuffix = StringUtils.isNotBlank(authzName) ? "@" + authzName : "";
                        log.error("Cannot authenticate user {} with profile [{}] connecting from '{}': {}",
                                userCredentials.getUsername() + userDomainSuffix,
                                profile,
                                ssoSession.getSourceAddr(),
                                ex.getMessage());
                        log.debug("Exception", ex);
                        SsoService.getSsoSession(request).setLoginMessage(ex.getMessage());
                    }
                    log.debug("Redirecting to LoginPage");
                    ssoSession.setReauthenticate(false);
                    ssoContext.registerSsoSessionById(SsoService.generateIdToken(), ssoSession);
                    if (StringUtils.isNotEmpty(ssoContext.getSsoDefaultProfile()) &&
                            Arrays.stream(request.getCookies()).noneMatch(c -> c.getName().equals("profile"))) {
                        Cookie cookie = new Cookie("profile", ssoContext.getSsoDefaultProfile());
                        cookie.setSecure("https".equalsIgnoreCase(request.getScheme()));
                        response.addCookie(cookie);
                    }
                    redirectUrl = request.getContextPath() + SsoConstants.INTERACTIVE_LOGIN_FORM_URI;
                }
            }
            if (redirectUrl != null) {
                response.sendRedirect(redirectUrl);
            }
        } catch (Exception ex) {
            SsoService.redirectToErrorPage(request, response, ex);
        }
    }

    private String authenticateUser(
            HttpServletRequest request,
            HttpServletResponse response,
            Credentials userCredentials) throws AuthenticationException {
        if (userCredentials == null || !SsoService.areCredentialsValid(request, userCredentials, true)) {
            throw new AuthenticationException(
                    ssoContext.getLocalizationUtils()
                            .localize(
                                    SsoConstants.APP_ERROR_INVALID_CREDENTIALS,
                                    (Locale) request.getAttribute(SsoConstants.LOCALE)));
        }
        try {
            log.debug("Authenticating user using credentials");
            Cookie cookie = new Cookie("profile", userCredentials.getProfile());
            cookie.setSecure("https".equalsIgnoreCase(request.getScheme()));
            response.addCookie(cookie);
            AuthenticationService.handleCredentials(
                    ssoContext,
                    request,
                    userCredentials);
            return request.getContextPath() + SsoConstants.INTERACTIVE_REDIRECT_TO_MODULE_URI;
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Internal Server Error: {}", ex.getMessage());
            log.debug("Exception", ex);
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private Credentials getUserCredentials(HttpServletRequest request) {
        String username = SsoService.getFormParameter(request, USERNAME);
        String password = SsoService.getFormParameter(request, PASSWORD);
        String profile = SsoService.getFormParameter(request, PROFILE);
        Credentials credentials;
        // The code is invoked from the login screen as well as when the user changes password.
        // If the login form parameters are not present the code has been invoked from change password flow and
        // we extract the credentials from the credentials saved to sso session.
        if (username == null || password == null || profile == null) {
            credentials = SsoService.getSsoSession(request).getTempCredentials();
        } else {
            credentials = new Credentials(username, password, profile, ssoContext.getSsoProfiles().contains(profile));
        }
        return credentials;
    }
}
