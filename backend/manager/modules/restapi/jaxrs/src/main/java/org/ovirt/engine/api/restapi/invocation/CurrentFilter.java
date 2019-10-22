/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.invocation;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;

/**
 * This filter is responsible for initializing and cleaning the information that is associated to the current request.
 */
public class CurrentFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "Correlation-Id";
    private static final String CORRELATION_ID_PARAM = "correlation_id";
    private static final Pattern INVALID_CORRELATION_ID_CHARACTERS_RE = Pattern.compile("[^0-9a-zA-Z_-]+");

    /**
     * The reference to the backend bean.
     */
    @SuppressWarnings("unused")
    @EJB(lookup = "java:global/engine/bll/Backend!org.ovirt.engine.core.common.interfaces.BackendLocal")
    private BackendLocal backend;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private static String filterCorrelationIdCharacters(String correlationId) {
        if (StringUtils.isNotEmpty(correlationId)) {
            correlationId = INVALID_CORRELATION_ID_CHARACTERS_RE.matcher(correlationId).replaceAll("");
            if (StringUtils.isNotEmpty(correlationId)) {
                return correlationId.substring(0, Math.min(correlationId.length(), 36));
            }
        }
        return null;
    }

    private static String getCorrelationId(HttpServletRequest request) {
        String correlationId = filterCorrelationIdCharacters(request.getHeader(CORRELATION_ID_HEADER));
        if (StringUtils.isEmpty(correlationId)) {
            correlationId = filterCorrelationIdCharacters(request.getParameter(CORRELATION_ID_PARAM));
        }
        if (StringUtils.isEmpty(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String sessionId = (String) request.getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
        if (sessionId == null) {
            throw new ServletException("Engine session missing");
        }

        Current current = new Current();
        current.setSessionId(sessionId);
        current.setApplicationMode(findApplicationMode(sessionId));
        current.setUser(findPrincipal(sessionId));
        current.setRoot(getRoot(request));
        current.setPrefix(getPrefix(request));
        current.setPath(getPath(request));
        current.setBackend(backend);
        String correlationId = getCorrelationId(request);
        current.getParameters().put(CORRELATION_ID_PARAM, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        CurrentManager.put(current);

        try {
            chain.doFilter(request, response);
        } finally {
            CurrentManager.remove();
        }
    }

    private ApplicationMode findApplicationMode(String sessionId) {
        GetConfigurationValueParameters parameters = new GetConfigurationValueParameters(
            ConfigValues.ApplicationMode,
            ConfigCommon.defaultConfigurationVersion
        );
        parameters.setSessionId(sessionId);
        QueryReturnValue result = backend.runPublicQuery( QueryType.GetConfigurationValue, parameters);
        return ApplicationMode.from(result.getReturnValue());
    }

    private DbUser findPrincipal(String sessionId) {
        QueryReturnValue result = backend.runPublicQuery(QueryType.GetDbUserBySession, new QueryParametersBase(sessionId));
        return result.getReturnValue();
    }

    private String getRoot(HttpServletRequest request) {
        StringBuilder buffer = new StringBuilder();
        String scheme = request.getScheme();
        buffer.append(scheme);
        buffer.append("://");
        String host = request.getServerName();
        buffer.append(host);
        int port = request.getServerPort();
        switch (scheme) {
        case "http":
            if (port != 80) {
                buffer.append(":");
                buffer.append(port);
            }
            break;
        case "https":
            if (port != 443) {
                buffer.append(":");
                buffer.append(port);
            }
            break;
        default:
            buffer.append(":");
            buffer.append(port);
        }
        return buffer.toString();
    }

    private String getPrefix(HttpServletRequest request) {
        return request.getContextPath();
    }

    private String getPath(HttpServletRequest request) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(request.getServletPath());
        String info = request.getPathInfo();
        if (info != null) {
            buffer.append(info);
        }
        return buffer.toString();
    }
}
