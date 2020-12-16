package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.sso.api.AuthenticationException;
import org.ovirt.engine.core.sso.api.Credentials;
import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.api.SsoContext;
import org.ovirt.engine.core.sso.service.AuthenticationService;
import org.ovirt.engine.core.sso.service.SsoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractiveChangePasswdServlet extends HttpServlet {

    private static final long serialVersionUID = -88168919566901736L;
    private static final String USERNAME = "username";
    private static final String CREDENTIALS = "credentials";
    private static final String CREDENTIALS_NEW1 = "credentialsNew1";
    private static final String CREDENTIALS_NEW2 = "credentialsNew2";
    private static final String PROFILE = "profile";

    private static Logger log = LoggerFactory.getLogger(InteractiveChangePasswdServlet.class);

    private SsoContext ssoContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        ssoContext = SsoService.getSsoContext(config.getServletContext());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("Entered InteractiveChangePasswdServlet");
        Credentials userCredentials = null;
        String redirectUrl;
        try {
            log.debug("User is not authenticated extracting credentials from request.");
            userCredentials = getUserCredentials(request);
            if (userCredentials == null) {
                throw new AuthenticationException(
                        ssoContext.getLocalizationUtils()
                                .localize(
                                        SsoConstants.APP_ERROR_UNABLE_TO_EXTRACT_CREDENTIALS,
                                        (Locale) request.getAttribute(SsoConstants.LOCALE)));
            }
            if (!userCredentials.getNewCredentials().equals(userCredentials.getConfirmedNewCredentials())) {
                throw new AuthenticationException(
                        ssoContext.getLocalizationUtils()
                                .localize(
                                        SsoConstants.APP_ERROR_PASSWORDS_DONT_MATCH,
                                        (Locale) request.getAttribute(SsoConstants.LOCALE)));
            }
            redirectUrl = changeUserPasswd(request, userCredentials);
        } catch (Exception ex) {
            String msg = String.format(
                    ssoContext.getLocalizationUtils()
                            .localize(
                                    SsoConstants.APP_ERROR_CHANGE_PASSWORD_FAILED,
                                    (Locale) request.getAttribute(SsoConstants.LOCALE)),
                    userCredentials == null ? "" : userCredentials.getUsernameWithProfile(),
                    ex.getMessage());
            log.error(msg);
            log.debug("Exception", ex);
            SsoService.getSsoSession(request).setChangePasswdMessage(msg);
            redirectUrl = request.getContextPath() + SsoConstants.INTERACTIVE_CHANGE_PASSWD_FORM_URI;
        }
        log.debug("Redirecting to url: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private String changeUserPasswd(HttpServletRequest request, Credentials userCredentials)
            throws AuthenticationException {
        log.debug("Calling Authn to change password for user '{}'.",
                userCredentials.getUsernameWithProfile());
        AuthenticationService.changePassword(ssoContext, request, userCredentials);
        SsoService.getSsoSession(request).setChangePasswdCredentials(null);
        if (SsoService.isUserAuthenticated(request)) {
            log.debug("User is authenticated updating password in SsoSession for password-access scope.");
            SsoService.persistUserPassword(request,
                    SsoService.getSsoSession(request),
                    userCredentials.getNewCredentials());
        } else {
            log.debug("User password change succeeded, redirecting to login page.");
            SsoService.getSsoSession(request)
                    .setLoginMessage(
                            ssoContext.getLocalizationUtils()
                                    .localize(
                                            SsoConstants.APP_MSG_CHANGE_PASSWORD_SUCCEEDED,
                                            (Locale) request.getAttribute(SsoConstants.LOCALE)));
        }
        return request.getContextPath() + SsoConstants.INTERACTIVE_LOGIN_URI;
    }

    private Credentials getUserCredentials(HttpServletRequest request) throws AuthenticationException {
        try {
            String username = SsoService.getFormParameter(request, USERNAME);
            String credentials = SsoService.getFormParameter(request, CREDENTIALS);
            String credentialsNew1 = SsoService.getFormParameter(request, CREDENTIALS_NEW1);
            String credentialsNew2 = SsoService.getFormParameter(request, CREDENTIALS_NEW2);
            String profile = SsoService.getFormParameter(request, PROFILE);
            return StringUtils.isNotEmpty(username) &&
                    StringUtils.isNotEmpty(credentials) &&
                    StringUtils.isNotEmpty(credentialsNew1) &&
                    StringUtils.isNotEmpty(credentialsNew2) &&
                    StringUtils.isNotEmpty(profile)
                            ? new Credentials(username, credentials, credentialsNew1, credentialsNew2, profile)
                            : null;
        } catch (Exception ex) {
            throw new AuthenticationException(
                    ssoContext.getLocalizationUtils()
                            .localize(
                                    SsoConstants.APP_ERROR_UNABLE_TO_EXTRACT_CREDENTIALS,
                                    (Locale) request.getAttribute(SsoConstants.LOCALE)),
                    ex);
        }
    }

}
