package org.ovirt.engine.core.aaa.servlet;

import java.io.IOException;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.uutils.net.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoLogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 9210030009170727847L;

    private static final Logger log = LoggerFactory.getLogger(SsoLogoutServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.debug("Entered SsoLogoutServlet");
        String token = null;
        try {
            String engineSessionId = (String) request.getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
            if (StringUtils.isEmpty(engineSessionId) && request.getSession(false) != null) {
                engineSessionId = (String) request.getSession(false).getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
            }
            if (StringUtils.isNotEmpty(engineSessionId)) {
                InitialContext ctx = new InitialContext();
                try {
                    QueryParametersBase params = new QueryParametersBase(engineSessionId);
                    params.setFiltered(true);
                    QueryReturnValue retValue = FiltersHelper.getBackend(ctx).runQuery(
                            QueryType.GetEngineSessionIdToken,
                            params
                    );
                    token = retValue.getReturnValue();
                    FiltersHelper.getBackend(ctx).runAction(
                            ActionType.LogoutSession,
                            new ActionParametersBase(engineSessionId)
                    );
                } finally {
                    ctx.close();
                }
            }
        } catch (Exception ex) {
            log.error("Unable to clear user session {}", ex.getMessage());
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            log.debug("Setting session attribute {}", FiltersHelper.Constants.LOGOUT_INPROGRESS);
            session.setAttribute(FiltersHelper.Constants.LOGOUT_INPROGRESS, true);
        }

        Map<String, Object> revokeResponse =  SsoOAuthServiceUtils.revoke(token);
        String error_description = (String) revokeResponse.get("error_description");
        String error = (String) revokeResponse.get("error");
        if (StringUtils.isNotEmpty(error_description)) {
            log.error("Unable to logout user: {}", error_description);
        }
        String url = String.format("%s://%s:%s%s/oauth2-callback", request.getScheme(),
                FiltersHelper.getRedirectUriServerName(request.getServerName()),
                request.getServerPort(),
                EngineLocalConfig.getInstance().getProperty("ENGINE_URI"));
        String redirectUri = new URLBuilder(url)
                .addParameter("error_description", StringUtils.defaultIfEmpty(error_description, ""))
                .addParameter("error", StringUtils.defaultIfEmpty(error, "")).build();

        if (session != null) {
            log.debug("Invalidating existing session");
            session.invalidate();
        }
        if (StringUtils.isEmpty(error_description) &&
                EngineLocalConfig.getInstance().getBoolean("ENGINE_SSO_ENABLE_EXTERNAL_SSO")) {
            String logoutUrl = String.format("%s://%s:%s%s", request.getScheme(),
                    FiltersHelper.getRedirectUriServerName(request.getServerName()),
                    request.getServerPort(),
                    EngineLocalConfig.getInstance().getProperty("ENGINE_SSO_EXTERNAL_SSO_LOGOUT_URI"));
            if (StringUtils.isNotEmpty(logoutUrl)) {
                redirectUri = new URLBuilder(logoutUrl).addParameter("logout", redirectUri).build();
            }
        }
        log.debug("Redirecting to {}", redirectUri);
        response.sendRedirect(redirectUri);
        log.debug("Exiting SsoLogoutServlet");
    }

}
