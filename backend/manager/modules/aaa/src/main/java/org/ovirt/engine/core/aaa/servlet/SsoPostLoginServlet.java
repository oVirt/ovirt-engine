package org.ovirt.engine.core.aaa.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.aaa.CreateUserSessionsError;
import org.ovirt.engine.core.aaa.SsoUtils;
import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateUserSessionParameters;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.uutils.net.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoPostLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 9210030009170727847L;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final int maxUserSessions;
    private boolean loginAsAdmin = false;
    private String postActionUrl;
    private String appScope;

    public SsoPostLoginServlet() {
        maxUserSessions = EngineLocalConfig.getInstance().getInteger("ENGINE_MAX_USER_SESSIONS");
    }

    @Override
    public void init() {
        String strVal = getServletConfig().getInitParameter("login-as-admin");
        if (strVal == null) {
            throw new RuntimeException("No login-as-admin init parameter specified for SsoPostLoginServlet.");
        }
        loginAsAdmin = Boolean.parseBoolean(strVal);
        postActionUrl = getServletContext().getInitParameter("post-action-url");
        if (postActionUrl == null) {
            throw new RuntimeException("No post-action-url init parameter specified for SsoPostLoginServlet.");
        }
        appScope = getServletContext().getInitParameter("app-scope");
        if (appScope == null) {
            // if app-scope is not specified in web.xml initialize it to ovirt-app-api to support older version of web-ui
            appScope = "ovirt-app-api";
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.debug("Entered SsoPostLoginServlet");
        String username;
        String profile = null;
        String authzName;
        InitialContext ctx = null;
        try {
            String error_description = request.getParameter("error_description");
            String error = request.getParameter("error");
            if (StringUtils.isNotEmpty(error_description) && StringUtils.isNotEmpty(error)){
                throw new RuntimeException(String.format("%s: %s", error, error_description));
            }
            String code = request.getParameter("code");
            if (StringUtils.isEmpty(code)){
                throw new RuntimeException("No authorization code found in request");
            }
            //appUrl not encoded
            String appUrl = request.getParameter("app_url");

            log.debug("Received app_url '{}'", appUrl);
            if (!SsoUtils.isDomainValid(appUrl)) {
                throw new RuntimeException(
                        "app_url domain differs from SSO_ENGINE_URL or SSO_ALTERNATE_ENGINE_FQDN domains");
            }
            Map<String, Object> jsonResponse = FiltersHelper.getPayloadForAuthCode(
                    code,
                    "ovirt-app-admin ovirt-app-portal ovirt-app-api",
                    URLEncoder.encode(postActionUrl, StandardCharsets.UTF_8));
            Map<String, Object> payload = (Map<String, Object>) jsonResponse.get("ovirt");

            username = (String) jsonResponse.get("user_id");
            int index = username.lastIndexOf("@");
            if (index != -1) {
                profile = username.substring(index + 1);
                username = username.substring(0, index);
            }
            authzName = (String) jsonResponse.get("user_authz");

            try {
                ctx = new InitialContext();
                ActionReturnValue queryRetVal = FiltersHelper.getBackend(ctx).runAction(ActionType.CreateUserSession,
                        new CreateUserSessionParameters(
                                (String) jsonResponse.get(SessionConstants.SSO_TOKEN_KEY),
                                (String) jsonResponse.get(SessionConstants.SSO_SCOPE_KEY),
                                appScope,
                                profile,
                                username,
                                (String) payload.get("principal_id"),
                                (String) payload.get("email"),
                                (String) payload.get("first_name"),
                                (String) payload.get("last_name"),
                                (String) payload.get("namespace"),
                                request.getRemoteAddr(),
                                (Collection<ExtMap>) payload.get("group_ids"),
                                loginAsAdmin));
                if (!queryRetVal.getSucceeded() ) {
                    if (queryRetVal.getActionReturnValue() == CreateUserSessionsError.NUM_OF_SESSIONS_EXCEEDED) {
                        throw new RuntimeException(String.format(
                                "Unable to login user %s@%s with profile [%s]" +
                                        " because the maximum number of allowed sessions %s is exceeded",
                                username,
                                authzName,
                                profile,
                                maxUserSessions));
                    }
                    throw new RuntimeException(String.format(
                            "The user %s@%s with profile [%s] is not authorized to perform login",
                            username,
                            authzName,
                            profile));
                } else {
                    HttpSession httpSession = request.getSession(true);
                    httpSession.setAttribute(
                            SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY,
                            queryRetVal.getActionReturnValue());
                    httpSession.setAttribute(
                            FiltersHelper.Constants.REQUEST_LOGIN_FILTER_AUTHENTICATION_DONE,
                            true);
                    log.debug("Redirecting to '{}'", appUrl);
                    response.sendRedirect(appUrl);
                }
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(
                        String.format("User login failure: %s@%s with profile [%s]", username, authzName, profile),
                        ex);
            } finally {
                try {
                    if (ctx != null) {
                        ctx.close();
                    }
                } catch (NamingException ex) {
                    log.error("Unable to close context", ex);
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            log.debug("User login failure", ex);
            String url = String.format("%s://%s:%s%s/", request.getScheme(),
                    FiltersHelper.getRedirectUriServerName(request.getServerName()),
                    request.getServerPort(),
                    EngineLocalConfig.getInstance().getProperty("ENGINE_URI"));
            response.sendRedirect(new URLBuilder(url)
                    .addParameter("error_description", StringUtils.defaultIfEmpty(ex.getMessage(), "Internal Server error"))
                    .addParameter("error", "server_error").build());
        }
    }
}
