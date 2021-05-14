package org.ovirt.engine.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.branding.BrandingManager;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.servlet.LocaleFilter;
import org.ovirt.engine.core.utils.servlet.UnsupportedLocaleHelper;
import org.ovirt.engine.core.uutils.net.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Servlet serves the welcome page to allow users to select either web admin or user portal.
 */
public class WelcomeServlet extends HttpServlet {
    /**
     * Generated UID.
     */
    private static final long serialVersionUID = 8289914264581273721L;

    /**
     * The request attribute keys describing the available locales.
     */
    private static final String LOCALE_KEYS = "localeKeys";

    /**
     * The request attribute containing the preamble html.
     */
    private static final String PREAMBLE = "preamble";

    /**
     * The request attribute containing the section html.
     */
    private static final String SECTIONS = "sections";

    /**
     * The request attribute containing the version.
     */
    private static final String VERSION = "version";

    /**
     * Back-end bean for database access.
     */
    private transient BackendLocal backend;

    /**
     * The branding manager.
     */
    private transient BrandingManager brandingManager;

    private static final Logger log = LoggerFactory.getLogger(WelcomeServlet.class);

    private static String identityScope = "ovirt-ext=auth:identity";
    private String engineUri;

    /**
     * Setter for the {@code BackendLocal} bean.
     *
     * @param backendLocal The bean
     */
    @EJB(beanInterface = BackendLocal.class,
            mappedName = "java:global/engine/bll/Backend!org.ovirt.engine.core.common.interfaces.BackendLocal")
    public void setBackend(final BackendLocal backendLocal) {
        this.backend = backendLocal;
    }

    @Override
    public void init() {
        init(BrandingManager.getInstance(), EngineLocalConfig.getInstance().getProperty(WelcomeUtils.ENGINE_URI));
    }

    /**
     * Init with the branding manager as a parameter.
     *
     * @param brandingManager The branding manager.
     */
    void init(final BrandingManager brandingManager, String engineUri) {
        this.brandingManager = brandingManager;
        this.engineUri = engineUri;
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
            ServletException {
        log.debug("Entered WelcomeServlet");

        String reauthenticate = (String) request.getSession(true).getAttribute(WelcomeUtils.REAUTHENTICATE);
        if (StringUtils.isEmpty(reauthenticate)) {
            Map<String, Object> deployedResponse = isSsoWebappDeployed();
            if (deployedResponse.containsKey(WelcomeUtils.ERROR_DESCRIPTION)) {
                request.getSession(true).setAttribute(WelcomeUtils.ERROR_DESCRIPTION,
                        deployedResponse.get(WelcomeUtils.ERROR_DESCRIPTION));
                request.getSession(true).setAttribute(WelcomeUtils.ERROR, deployedResponse.get(WelcomeUtils.ERROR));
            }
        }

        String authCode = (String) request.getSession(true).getAttribute(WelcomeUtils.AUTH_CODE);
        String token = (String) request.getSession(true).getAttribute(WelcomeUtils.TOKEN);
        String errorDescription = (String) request.getSession(true).getAttribute(WelcomeUtils.ERROR_DESCRIPTION);

        if (StringUtils.isNotEmpty(token) && !isSessionValid(request, token)) {
            request.getSession(true).removeAttribute(WelcomeUtils.TOKEN);
            request.getSession(true).removeAttribute(WelcomeUtils.SSO_USER);
            request.getSession(true).removeAttribute(WelcomeUtils.CAPABILITY_CREDENTIALS_CHANGE);
            token = "";
        }
        if (authCode == null && StringUtils.isEmpty(errorDescription) && StringUtils.isEmpty(reauthenticate)) {
            if (StringUtils.isNotEmpty(request.getParameter(WelcomeUtils.ERROR)) &&
                    !WelcomeUtils.ERR_OVIRT_CODE_NOT_AUTHENTICATED.equals(request.getParameter(WelcomeUtils.ERROR))) {
                request.getSession(true).setAttribute(WelcomeUtils.ERROR_DESCRIPTION,
                        request.getParameter(WelcomeUtils.ERROR_DESCRIPTION));
                request.getSession(true).setAttribute(WelcomeUtils.ERROR, request.getParameter(WelcomeUtils.ERROR));
            }
            String url = WelcomeUtils.getLoginUrl(engineUri, identityScope);
            log.debug("redirecting to {}", url);
            response.sendRedirect(url);
        } else {
            request.getSession(true).removeAttribute(WelcomeUtils.REAUTHENTICATE);
            log.debug("Displaying Welcome Page");
            try {
                setUserNameForMenu(request, token);
            } catch (Exception ex) {
                log.debug("Unable to set request attributed for user menu", ex);
                log.error("Unable to set request attributed for user menu: {}", ex.getMessage());
            }
            request.setAttribute(LOCALE_KEYS, UnsupportedLocaleHelper.getDisplayedLocales(LocaleFilter.getLocaleKeys()));
            String oVirtVersion = backend.runPublicQuery(QueryType.GetConfigurationValue,
                    new GetConfigurationValueParameters(ConfigValues.ProductRPMVersion,
                            ConfigCommon.defaultConfigurationVersion)).getReturnValue();
            request.setAttribute("sso_credential_change_url", getCredentialsChangeUrl(request));
            request.setAttribute(VERSION, oVirtVersion != null ? oVirtVersion : "myVersion");

            Locale locale = (Locale) request.getAttribute(LocaleFilter.LOCALE);
            request.setAttribute(PREAMBLE, brandingManager.getWelcomePreambleSection(locale));
            request.setAttribute(SECTIONS, brandingManager.getWelcomeSections(locale));

            log.debug("Including to ovirt-engine.jsp");
            RequestDispatcher dispatcher = request.getRequestDispatcher(WelcomeUtils.WELCOME_PAGE_JSP_URI);
            response.setContentType("text/html;charset=UTF-8");
            if (dispatcher != null) {
                dispatcher.include(request, response);
            }
        }
        log.debug("Exiting WelcomeServlet");
    }

    private void setUserNameForMenu(HttpServletRequest request, String token) {
        try {
           log.debug("setting request attributes for User Menu");
            if (StringUtils.isNotEmpty(token)) {
                Map<String, Object> userInfoMap = SsoOAuthServiceUtils.getTokenInfo(token);
                String username = getCurrentSsoSessionUser(request, userInfoMap);
                if (StringUtils.isNotEmpty(username)) {
                    request.getSession(true).setAttribute(WelcomeUtils.SSO_USER, username);
                    request.getSession(true).setAttribute(WelcomeUtils.CAPABILITY_CREDENTIALS_CHANGE,
                            getChangePasswordEnabled(userInfoMap));
                    request.getSession(true).setAttribute(WelcomeUtils.ENGINE_SSO_ENABLE_EXTERNAL_SSO,
                            EngineLocalConfig.getInstance().getBoolean("ENGINE_SSO_ENABLE_EXTERNAL_SSO"));
                }
            }
        } catch (Exception e) {
            log.error("Unable to get session user", e);
        } finally {
            request.getSession(true).removeAttribute(WelcomeUtils.AUTH_CODE);
        }
    }

    public String getCredentialsChangeUrl(HttpServletRequest request) throws MalformedURLException {
        return new URLBuilder(FiltersHelper.getEngineSsoUrl(request),
                WelcomeUtils.CREDENTIALS_CHANGE_FORM_URI).build();
    }

    public String getCurrentSsoSessionUser(HttpServletRequest request, Map<String, Object> userInfoMap) {
        String username  = null;
        if (userInfoMap.containsKey(WelcomeUtils.ERROR_DESCRIPTION)) {
            request.getSession(true).setAttribute(WelcomeUtils.ERROR_DESCRIPTION,
                    userInfoMap.get(WelcomeUtils.ERROR_DESCRIPTION));
            request.getSession(true).setAttribute(WelcomeUtils.ERROR, userInfoMap.get(WelcomeUtils.ERROR));
        } else {
            String userId = getUserId(userInfoMap);
            String userAuthz = (String) userInfoMap.get(WelcomeUtils.JSON_USER_AUTHZ);
            username = userId + "@" + userAuthz;
            log.debug("Got current user {} for session", username);
        }
        return username;
    }

    private String getUserId(Map<String, Object> userInfoMap) {
        String userIdWithProfile = (String) userInfoMap.get(WelcomeUtils.JSON_USER_ID);
        String userId = null;
        int index = userIdWithProfile.lastIndexOf("@");
        if (index != -1) {
            userId = userIdWithProfile.substring(0, index);
        }
        return userId;
    }

    public boolean getChangePasswordEnabled(Map<String, Object> userInfoMap) {
        return (boolean) ((Map<String, Object>) userInfoMap.get("ovirt"))
                .get(WelcomeUtils.CAPABILITY_CREDENTIALS_CHANGE);
    }

    public Map<String, Object> isSsoWebappDeployed() {
        return SsoOAuthServiceUtils.isSsoDeployed();
    }

    public boolean isSessionValid(HttpServletRequest request, String token) {
        boolean isValid = false;
        if (StringUtils.isNotEmpty(token)) {
            Map<String, Object> response = SsoOAuthServiceUtils.getTokenInfo(token, "ovirt-ext=token-info:validate");
            isValid = !response.containsKey(WelcomeUtils.ERROR_DESCRIPTION);
            if (response.containsKey(WelcomeUtils.ERROR_DESCRIPTION) &&
                    !WelcomeUtils.ERR_CODE_INVALID_GRANT.equals(response.get(WelcomeUtils.ERROR))) {
                request.getSession(true).setAttribute(WelcomeUtils.ERROR_DESCRIPTION,
                        response.get(WelcomeUtils.ERROR_DESCRIPTION));
                request.getSession(true).setAttribute(WelcomeUtils.ERROR, response.get(WelcomeUtils.ERROR));
                log.error("Session not valid session id = " + token, response.get(WelcomeUtils.ERROR_DESCRIPTION));
            }
        }
        return isValid;
    }

}
