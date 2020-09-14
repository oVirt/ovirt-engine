package org.ovirt.engine.core.aaa.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ovirt.engine.core.common.constants.SessionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionValidationFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(SessionValidationFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        boolean doFilter = false;
        try {
            String requestEngineSession = (String)request.getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
            if (requestEngineSession != null) {
                if (!FiltersHelper.isSessionValid(requestEngineSession)) {
                    request.removeAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
                }
            }

            HttpSession httpSession = ((HttpServletRequest) request).getSession(false);
            if (httpSession != null) {
                String engineSession = (String) httpSession.getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
                if (engineSession != null) {
                    if (!FiltersHelper.isSessionValid(engineSession)) {
                        try {
                            httpSession.invalidate();
                        } catch (IllegalStateException e) {
                            // ignore
                        }
                    }
                }
            }

            doFilter = true;
        } catch (Exception ex) {
            log.error("An error has occurred while session validation.", ex);
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        if (doFilter) {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }

}
