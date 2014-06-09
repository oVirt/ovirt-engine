package org.ovirt.engine.core.aaa.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.ovirt.engine.core.common.constants.SessionConstants;

public class SessionMgmtFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        String engineSessionId = (String) request.getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
        if (engineSessionId != null) {
            ((HttpServletRequest) request).getSession(true).setAttribute(
                    SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY,
                    engineSessionId
                    );
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
