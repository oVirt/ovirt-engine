package org.ovirt.engine.core.aaa.filters;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RestApiSessionMgmtFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RestApiSessionMgmtFilter.class);

    private static final int MINIMAL_SESSION_TTL = 1;
    private static final int SECONDS_IN_MINUTE = 60;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        String engineSessionId = (String) request.getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
        if (engineSessionId == null) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                engineSessionId = (String) session.getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
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
                int ttlValue = Integer.parseInt(req.getHeader("Session-TTL")) * SECONDS_IN_MINUTE;
                if (ttlValue >= MINIMAL_SESSION_TTL) {
                    session.setMaxInactiveInterval(ttlValue);
                }
            } catch (NumberFormatException ex) {
                // ignore error
            }
        }

        chain.doFilter(request, response);

        try {
            if (FiltersHelper.isAuthenticated(req)) {
                if ((prefer & FiltersHelper.PREFER_PERSISTENCE_AUTH) != 0) {
                    HttpSession session = req.getSession(false);
                    if (session != null) {
                        ((HttpServletResponse) response).addHeader(FiltersHelper.Constants.HEADER_JSESSIONID_COOKIE,
                                session.getId());
                    }
                } else {
                    InitialContext ctx = new InitialContext();
                    try {
                        FiltersHelper.getBackend(ctx).runAction(
                                VdcActionType.LogoutSession,
                                new VdcActionParametersBase(engineSessionId)
                                );
                        HttpSession session = req.getSession(false);
                        if (session != null) {
                            session.invalidate();
                        }
                    } finally {
                        ctx.close();
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error in REST-API session management. ", ex);
            throw new ServletException(ex);
        }
    }

    @Override
    public void destroy() {
    }

}
