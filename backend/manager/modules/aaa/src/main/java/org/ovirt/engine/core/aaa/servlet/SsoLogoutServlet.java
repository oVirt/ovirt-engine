package org.ovirt.engine.core.aaa.servlet;

import java.io.IOException;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.uutils.net.URLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoLogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 9210030009170727847L;

    private static final Logger log = LoggerFactory.getLogger(SsoLogoutServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
                    VdcQueryParametersBase params = new VdcQueryParametersBase(engineSessionId);
                    params.setFiltered(true);
                    VdcQueryReturnValue retValue = FiltersHelper.getBackend(ctx).runQuery(
                            VdcQueryType.GetEngineSessionIdToken,
                            params
                    );
                    token = retValue.getReturnValue();
                    FiltersHelper.getBackend(ctx).runAction(
                            VdcActionType.LogoutSession,
                            new VdcActionParametersBase(engineSessionId)
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
        String error = (String) revokeResponse.get("error");
        String error_code = (String) revokeResponse.get("error_code");
        if (StringUtils.isNotEmpty(error)) {
            log.error("Unable to logout user: {}", error);
        }
        String url = String.format("%s://%s:%s%s/oauth2-callback", request.getScheme(),
                FiltersHelper.getRedirectUriServerName(request.getServerName()),
                request.getServerPort(),
                EngineLocalConfig.getInstance().getProperty("ENGINE_URI"));
        String redirectUri = new URLBuilder(url)
                .addParameter("error", StringUtils.defaultIfEmpty(error, ""))
                .addParameter("error_code", StringUtils.defaultIfEmpty(error_code, "")).build();

        if (session != null) {
            log.debug("Invalidating existing session");
            session.invalidate();
        }
        log.debug("Redirecting to {}", redirectUri);
        response.sendRedirect(redirectUri);
        log.debug("Exiting SsoLogoutServlet");
    }

}
