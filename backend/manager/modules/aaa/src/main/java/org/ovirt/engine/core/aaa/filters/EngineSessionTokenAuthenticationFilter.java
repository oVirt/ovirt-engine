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

public class EngineSessionTokenAuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (!FiltersHelper.isAuthenticated(req)) {
            String token = req.getHeader(FiltersHelper.Constants.HEADER_ENGINE_AUTH_TOKEN);
            if (token != null) {
                request.setAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY, FiltersHelper.getTokenContent(token));
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
