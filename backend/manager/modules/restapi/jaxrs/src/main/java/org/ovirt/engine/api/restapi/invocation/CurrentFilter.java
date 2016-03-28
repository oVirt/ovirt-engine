/*
Copyright (c) 2015-2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.invocation;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;

/**
 * This filter is responsible for initializing and cleaning the information that is associated to the current request.
 */
public class CurrentFilter implements Filter {
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
        CurrentManager.put(current);

        try {
            chain.doFilter(request, response);
        }
        finally {
            CurrentManager.remove();
        }
    }

    private ApplicationMode findApplicationMode(String sessionId) {
        GetConfigurationValueParameters parameters = new GetConfigurationValueParameters(
            ConfigurationValues.ApplicationMode,
            ConfigCommon.defaultConfigurationVersion
        );
        parameters.setSessionId(sessionId);
        VdcQueryReturnValue result = backend.runPublicQuery( VdcQueryType.GetConfigurationValue, parameters);
        return ApplicationMode.from(result.getReturnValue());
    }

    private DbUser findPrincipal(String sessionId) {
        VdcQueryReturnValue result = backend.runPublicQuery(VdcQueryType.GetDbUserBySession, new VdcQueryParametersBase(sessionId));
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
