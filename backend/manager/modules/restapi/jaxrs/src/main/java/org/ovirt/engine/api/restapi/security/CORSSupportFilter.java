/*
* Copyright (c) 2015 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.restapi.security;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ebaysf.web.cors.CORSFilter;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter implements CORS (Cross Site Resource Sharing). In does so delegating all the hard work to an existing
 * filter developed by eBay (see <a href="https://github.com/ebay/cors-filter">here</a>). The only purpose of this
 * class is to get the configuration from the backend and pass it to the eBay filter.
 */
@SuppressWarnings("unused")
public class CORSSupportFilter implements Filter {
    /**
     * The log used by the filter.
     */
    private static final Logger log = LoggerFactory.getLogger(CORSSupportFilter.class);

    /**
     * We need access to the backend in order to get the values of the configuration parameters.
     */
    @EJB(lookup = "java:global/engine/bll/Backend!org.ovirt.engine.core.common.interfaces.BackendLocal")
    private BackendLocal backend;

    /**
     * We must perform lazy initialization because the application server runs the {@code init} method in one of the
     * MSC (Modular Service Container) threads, and it may happen that it decides to run initialization of the RESTAPI
     * and the backend in the same thread, first the RESTAPI and then the backend. If this happens any attempt to lookup
     * the backend bean will cause a deadlock. So we use this flag to indicate if initialization has been performed
     * already, and to perform it lazily as needed.
     */
    private volatile boolean initialized = false;

    /**
     * We need to save the filter configuration, for use during lazy initialization.
     */
    private FilterConfig config;

    /**
     * This is the filter that perform all the actual work, we just delegate calls to it.
     */
    private Filter delegate;

    @Override
    public void init(final FilterConfig config) throws ServletException {
        this.config = config;
    }

    @Override
    public void destroy() {
        if (delegate != null) {
            delegate.destroy();
        }
    }

    private void lazyInit() throws ServletException {
        // Check if the CORS support is enabled:
        final Boolean enabled = (Boolean) getBackendParameter(ConfigurationValues.CORSSupport);
        if (enabled == null || !enabled) {
            log.info("CORS support is disabled.");
            return;
        }

        // Get the allowed origins from the backend configuration:
        final String allowedOrigins = (String) getBackendParameter(ConfigurationValues.CORSAllowedOrigins);
        if (StringUtils.isEmpty(allowedOrigins)) {
            log.warn(
                "The CORS support has been enabled, but the list of allowed origins is empty. This means that CORS " +
                "support will actually be disabled."
            );
            return;
        }
        log.info("CORS support is enabled for origins \"{}\".", allowedOrigins);

        // Populate the parameters for the delegate:
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(CORSFilter.PARAM_CORS_ALLOWED_METHODS, "GET,POST,PUT,DELETE");
        parameters.put(CORSFilter.PARAM_CORS_ALLOWED_HEADERS, "Accept,Authorization,Content-Type");
        parameters.put(CORSFilter.PARAM_CORS_ALLOWED_ORIGINS, allowedOrigins);

        // Add all the parameters of this filter to those passed to the delegate, so that the user can override the
        // configuration modifying the web.xml file:
        final Enumeration<String> names = config.getInitParameterNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final String value = config.getInitParameter(name);
            parameters.put(name, value);
        }

        // Create the delegate and initialize with the prepared parameters:
        delegate = new CORSFilter();
        delegate.init(
            new FilterConfig() {
                @Override
                public String getFilterName() {
                    return config.getFilterName();
                }

                @Override
                public ServletContext getServletContext() {
                    return config.getServletContext();
                }

                @Override
                public String getInitParameter(String name) {
                    return parameters.get(name);
                }

                @Override
                public Enumeration<String> getInitParameterNames() {
                    return Collections.enumeration(parameters.keySet());
                }
            }
        );
    }

    private Object getBackendParameter(final ConfigurationValues key) throws ServletException {
        final GetConfigurationValueParameters parameters = new GetConfigurationValueParameters();
        parameters.setConfigValue(key);
        parameters.setVersion(ConfigCommon.defaultConfigurationVersion);
        VdcQueryReturnValue value = backend.runPublicQuery(VdcQueryType.GetConfigurationValue, parameters);
        if (!value.getSucceeded()) {
            throw new ServletException("Can't get value of backend parameter \"" + key + "\".");
        }
        return value.getReturnValue();
    }

    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        // Perform lazy initialization, if needed:
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    lazyInit();
                    initialized = true;
                }
            }
        }

        // Do the filtering if needed:
        if (delegate != null) {
            delegate.doFilter(request, response, chain);
        }
        else {
            chain.doFilter(request, response);
        }
    }
}
