package org.ovirt.engine.core.aaa.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.common.action.CreateUserSessionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.uutils.net.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoPostLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 9210030009170727847L;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private boolean loginAsAdmin = false;
    private String postActionUrl;

    @Override
    public void init() throws ServletException {
        String strVal = getServletConfig().getInitParameter("login-as-admin");
        if (strVal == null) {
            throw new RuntimeException("No login-as-admin init parameter specified for SsoPostLoginServlet.");
        }
        loginAsAdmin = Boolean.parseBoolean(strVal);
        postActionUrl = getServletContext().getInitParameter("post-action-url");
        if (postActionUrl == null) {
            throw new RuntimeException("No post-action-url init parameter specified for SsoPostLoginServlet.");
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = null;
        String profile = null;
        InitialContext ctx = null;
        try {
            String error = request.getParameter("error");
            String error_code = request.getParameter("error_code");
            if (StringUtils.isNotEmpty(error) && StringUtils.isNotEmpty(error_code)){
                throw new RuntimeException(String.format("%s: %s", error_code, error));
            }
            String code = request.getParameter("code");
            if (StringUtils.isEmpty(code)){
                throw new RuntimeException("No authorization code found in request");
            }
            String stateInSession = (String) request.getSession(true).getAttribute("state");
            String state = request.getParameter("state");
            if (StringUtils.isEmpty(stateInSession) || !stateInSession.equals(state)) {
                throw new RuntimeException("Request state does not match session state.");
            }
            Map<String, Object> jsonResponse = FiltersHelper.getPayloadForAuthCode(
                    code,
                    "ovirt-app-admin ovirt-app-portal ovirt-app-api",
                    URLEncoder.encode(postActionUrl, "UTF-8"));
            Map<String, Object> payload = (Map<String, Object>) jsonResponse.get("ovirt");

            username = (String) jsonResponse.get("user_id");
            profile = "";
            int index = username.lastIndexOf("@");
            if (index != -1) {
                profile = username.substring(index+1);
                username = username.substring(0, index);
            }

            try {
                ctx = new InitialContext();
                VdcReturnValueBase queryRetVal = FiltersHelper.getBackend(ctx).runAction(VdcActionType.CreateUserSession,
                        new CreateUserSessionParameters(
                                (String) jsonResponse.get(SessionConstants.SSO_TOKEN_KEY),
                                profile,
                                username,
                                (String) payload.get("principal_id"),
                                (String) payload.get("email"),
                                request.getRemoteAddr(),
                                (Collection<ExtMap>) payload.get("group_ids"),
                                loginAsAdmin));
                if (!queryRetVal.getSucceeded() ) {
                    throw new RuntimeException(String.format("The user %s@%s is not authorized to perform login", username, profile));
                } else {
                    HttpSession httpSession = request.getSession(true);
                    httpSession.setAttribute(
                            SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY,
                            queryRetVal.getActionReturnValue());
                    httpSession.setAttribute(
                            FiltersHelper.Constants.REQUEST_LOGIN_FILTER_AUTHENTICATION_DONE,
                            true);
                    response.sendRedirect((String) httpSession.getAttribute("app_url"));
                }
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(String.format("User login failure: %s", username), ex);
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
                    .addParameter("error", StringUtils.defaultIfEmpty(ex.getMessage(), "Internal Server error"))
                    .addParameter("error_code", "server_error").build());
        }
    }
}
