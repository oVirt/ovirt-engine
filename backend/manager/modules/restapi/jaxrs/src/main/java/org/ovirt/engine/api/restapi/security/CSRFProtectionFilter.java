/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.security;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HeaderElement;
import org.apache.http.message.BasicHeaderValueParser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter implements CSRF protection. In order to activate the protection the client system has to activate it
 * adding the {@code csrf-protection} element to the {@code Prefer} header sent in the first request for the session:
 *
 * <pre>
 * GET /ovirt-engine/api HTTP/1.1
 * Authorization: Basic P/c1qcSSGuTlxUCTEUCosZfZ
 * Host: ovirt.example.com
 * Prefer: persistent-auth, csrf-protection
 * </pre>
 *
 * The server will then require that the session identifier is sent with every request, inside the {@code JSESSIONID}
 * header:
 *
 * <pre>
 * GET /ovirt-engine/api HTTP/1.1
 * Cookie: JSESSIONID=y+FXYivGm2rdajrNhTRatNjl
 * Prefer: persistent-auth, csrf-protection
 * JSESSIONID: y+FXYivGm2rdajrNhTRatNjl
 * </pre>
 *
 * Requests for sessions where protection has been enabled will be checked. If the session identifier header isn't
 * provided or incorrect they will be rejected with code 403 (forbidden) and logged as warnings.
 */
@SuppressWarnings("unused")
public class CSRFProtectionFilter implements Filter {
    /**
     * The log used by the filter.
     */
    private static final Logger log = LoggerFactory.getLogger(CSRFProtectionFilter.class);

    /**
     * The name of the header that contains preferences.
     */
    private static final String PREFER_HEADER = "Prefer";

    /**
     * The name of the header element that is used to request protection.
     */
    private static final String PREFER_ELEMENT = "csrf-protection";

    /**
     * The name of the header that contains the session id.
     */
    private static final String SESSION_ID_HEADER = "JSESSIONID";

    /**
     * The name of the session attribute that contains the boolean flag that indicates if the protection is enabled
     * for the session.
     */
    private static final String ENABLED_ATTRIBUTE = CSRFProtectionFilter.class.getName() + ".enabled";

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // If protection is globally disabled then we don't need to do anything else, jump directly to the next filter
        // in the chain:
        boolean enabled = Config.getValue(ConfigValues.CSRFProtection);
        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }

        // If there is already a session then we need to process it immediately, before letting other filters or the
        // application see or touch the request:
        HttpSession session = request.getSession(false);
        if (session != null) {
            doFilterExistingSession(session, request, response, chain);
            return;
        }

        // At this point we know that protection is globally enabled, and that there isn't a session already created. So
        // we should first let the other filters and the application do their work. As a result a new session may be
        // created. In that case we need to check if protection has been requested for that session and store the result
        // for use in future requests.
        try {
            chain.doFilter(request, response);
        } finally {
            session = request.getSession(false);
            if (session != null) {
                enabled = isProtectionRequested(request);
                session.setAttribute(ENABLED_ATTRIBUTE, enabled);
            }
        }
    }

    private void doFilterExistingSession(HttpSession session, HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        // Check if the protection is enabled for this session, if it isn't then jump to the next filter:
        boolean enabled = (Boolean) session.getAttribute(ENABLED_ATTRIBUTE);
        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }

        // Check if the request contains a session id header, if it doesn't then it must be rejected immediately:
        String sessionIdHeader = request.getHeader(SESSION_ID_HEADER);
        if (sessionIdHeader == null) {
            log.warn(
                "Request for path \"{}\" from IP address {} has been rejected because CSRF protection is enabled " +
                "for the session but the session id header \"{}\" hasn't been provided.",
                request.getContextPath() + request.getPathInfo(),
                request.getRemoteAddr(),
                SESSION_ID_HEADER
            );
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Check if the actual session id matches the session id header:
        String actualSessionId = session.getId();
        if (!sessionIdHeader.equals(actualSessionId)) {
            log.warn(
                "Request for path \"{}\" from IP address {} has been rejected because CSRF protection is enabled " +
                "for the session but the value of the session id header \"{}\" doesn't match the actual session " +
                "id.",
                request.getContextPath() + request.getPathInfo(),
                request.getRemoteAddr(),
                SESSION_ID_HEADER
            );
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Everything is OK, let the request go to the next filter:
        chain.doFilter(request, response);
    }

    /**
     * Checks if the headers contained in the given request indicate that the user wants to enable protection. This
     * means checking if the {@code Prefer} header exists and has at least one {@code csrf-protection} element. For
     * example:
     *
     * <pre>
     * GET /ovirt-engine/api HTTP/1.1
     * Host: ovirt.example.com
     * Prefer: persistent-auth, csrf-protection
     * </pre>
     *
     * @param request the HTTP request to check
     * @return {@code true} if the request contains headers that indicate that protection should be enabled,
     *   {@code false} otherwise
     */
    private boolean isProtectionRequested(HttpServletRequest request) {
        Enumeration<String> headerValues = request.getHeaders(PREFER_HEADER);
        while (headerValues.hasMoreElements()) {
            String headerValue = headerValues.nextElement();
            HeaderElement[] headerElements = BasicHeaderValueParser.parseElements(headerValue, null);
            for (HeaderElement headerElement : headerElements) {
                String elementName = headerElement.getName();
                if (PREFER_ELEMENT.equalsIgnoreCase(elementName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
