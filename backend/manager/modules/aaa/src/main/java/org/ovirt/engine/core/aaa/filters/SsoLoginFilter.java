package org.ovirt.engine.core.aaa.filters;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoLoginFilter implements Filter {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private String loginUrl;

    @Override
    public void init(FilterConfig filterConfig) {
        loginUrl = filterConfig.getInitParameter("login-url");
        if (loginUrl == null) {
            throw new RuntimeException("No login-url init parameter specified for SsoLoginFilter.");
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        log.debug("Entered SsoLoginFilter");
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        if (req.getSession(false) == null || req.getSession(false).getAttribute(FiltersHelper.Constants.LOGOUT_INPROGRESS) == null) {
            StringBuffer requestURL = req.getRequestURL();
            if (StringUtils.isNotEmpty(req.getQueryString())) {
                requestURL.append("?").append(req.getQueryString());
            }
            if (!FiltersHelper.isAuthenticated(req) || !FiltersHelper.isSessionValid((HttpServletRequest) request)) {
                String url = String.format("%s%s&app_url=%s&locale=%s",
                        req.getServletContext().getContextPath(),
                        loginUrl,
                        URLEncoder.encode(requestURL.toString(), StandardCharsets.UTF_8),
                        request.getAttribute("locale").toString());
                log.debug("Redirecting to {}", url);
                res.sendRedirect(url);
            } else {
                log.debug("Already logged in, executing next filter in chain.");
                chain.doFilter(request, response);
            }
        }
        log.debug("Exiting SsoLoginFilter");
    }

    @Override
    public void destroy() {
    }

}
