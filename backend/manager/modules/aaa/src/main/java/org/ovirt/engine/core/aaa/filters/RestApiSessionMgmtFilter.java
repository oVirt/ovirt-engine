package org.ovirt.engine.core.aaa.filters;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.SetSesssionSoftLimitCommandParameters;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestApiSessionMgmtFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RestApiSessionMgmtFilter.class);

    private static final int MINIMAL_SESSION_TTL = 1;
    private static final String BEARER = "Bearer";

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        try {
            HttpServletRequest req = (HttpServletRequest) request;

            String engineSessionId = (String) request.getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
            if (engineSessionId == null) {
                HttpSession session = req.getSession(false);
                if (session != null) {
                    engineSessionId =
                            (String) session.getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
                    if (engineSessionId != null) {
                        request.setAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY, engineSessionId);
                    }
                }
            }

            if (engineSessionId == null) {
                throw new ServletException("No engine session");
            }

            int prefer = FiltersHelper.getPrefer(req);
            if ((prefer & FiltersHelper.PREFER_PERSISTENCE_AUTH) != 0) {
                HttpSession session = req.getSession(true);
                session.setAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY, engineSessionId);
                try {
                    int ttlMinutes = Integer.parseInt(req.getHeader("Session-TTL"));
                    if (ttlMinutes >= MINIMAL_SESSION_TTL) {
                        // For new sessions:
                        if (isNewSession(req)) {
                            // Save Session-TTL on the HTTP session (in seconds).
                            session.setMaxInactiveInterval((int) TimeUnit.MINUTES.toSeconds(ttlMinutes));
                            // Save Session-TTL in the Engine.
                            setEngineSessionSoftLimit(engineSessionId, ttlMinutes);
                        }
                    }
                } catch (NumberFormatException ex) {
                    // ignore error
                }
            }

            chain.doFilter(request, response);

            if (FiltersHelper.isAuthenticated(req)) {
                String headerValue = req.getHeader(FiltersHelper.Constants.HEADER_AUTHORIZATION);
                if ((headerValue == null || !headerValue.startsWith(BEARER)) &&
                        (prefer & FiltersHelper.PREFER_PERSISTENCE_AUTH) == 0) {
                    InitialContext ctx = new InitialContext();
                    try {
                        FiltersHelper.getBackend(ctx).runAction(
                                ActionType.LogoutSession,
                                new ActionParametersBase(engineSessionId)
                                );
                        HttpSession session = req.getSession(false);
                        if (session != null) {
                            try {
                                session.invalidate();
                            } catch (IllegalStateException e) {
                                // ignore
                            }
                        }
                    } finally {
                        ctx.close();
                    }
                }
            }
        } catch (NamingException e) {
            log.error("REST-API session failed: {}", e.getMessage());
            log.debug("Exception", e);
            throw new ServletException(e);
        }
    }

    /*
     * A session is considered new if this request has resulted in a log-in. At this point in time we are after the
     * log-in, but we can know if it took place by the value of 'ovirt_aaa_login_filter_authentication_done' attribute.
     * LoginFilter sets 'true' for this attribute and when a log-in is performed.
     */
    private boolean isNewSession(HttpServletRequest req) {
        return req.getAttribute(FiltersHelper.Constants.REQUEST_LOGIN_FILTER_AUTHENTICATION_DONE) != null
                && (boolean) req.getAttribute(FiltersHelper.Constants.REQUEST_LOGIN_FILTER_AUTHENTICATION_DONE);
    }

    private void setEngineSessionSoftLimit(String engineSessionId, int ttlValue) throws IOException, NamingException {

        InitialContext context = new InitialContext();
        try {
            FiltersHelper.getBackend(context).runAction(ActionType.SetSesssionSoftLimit,
                    new SetSesssionSoftLimitCommandParameters(engineSessionId, ttlValue));
        } finally {
            try {
                context.close();
            } catch (NamingException e) {
                log.error("Error in REST-API session management. 'Context' object could not be manually closed. " +
                        "This is a cleanup error only; it does not disturb application flow", e);
            }
        }
    }

    @Override
    public void destroy() {
    }

}
