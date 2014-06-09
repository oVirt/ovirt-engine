package org.ovirt.engine.core.aaa.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class RestApiSessionValidationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (!FiltersHelper.isPersistentAuth(req)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
                session = null;
            }
        }
        chain.doFilter(request, response);

    }

    @Override
    public void destroy() {
    }

}
