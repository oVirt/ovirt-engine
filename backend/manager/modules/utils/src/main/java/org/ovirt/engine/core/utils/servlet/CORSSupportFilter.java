/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.core.utils.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.GetDefaultAllowedOriginsQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
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

    private static final String DEFAULT_ORIGINS_SUFFIXES = "defaultOriginsSuffixes";
    private static final Set<String> EMPTY_SET = new HashSet<>();

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

    /**
     * Time when previous call of createDelegate() happened (in ms).
     */
    private long lastInitializationTime = 0L;

    /**
     * The createDelegate() is not called more then once per time period of delayBeforeReinit (in ms).
     */
    private long delayBeforeReinit = 60 * 1000L;

    /**
     * True, if the CORSSupport is allowed in the configuration (by engine-config).
     *
     * If true, the filter will handle CORS request headers accordingly.
     * Otherwise, CORS is disabled.
     */
    private Boolean enabled;

    /**
     * True, if the CORSAllowDefaultOrigins is allowed in the configuration (by engine-config).
     *
     * If true nad CORS is enabled, the filter will consider all configured hosts as allowed origins.
     * Otherwise, only values from CORSAllowedOrigins are taken.
     */
    private Boolean enabledDefaultOrigins;

    /**
     * Suffixes to be appended to all default origins. Usually port(s) can be provided.
     * Effective only if the enabledDefaultOrigins is true.
     *
     * Can be configured via `engine-config -s CORSDefaultOriginSuffixes=[comma separated list]`
     * Example:
     *   engine-config -s 'CORSDefaultOriginSuffixes=:9090,:1234'
     */
    private Set<String> defaultOriginsSuffixes;

    /**
     * Keep previous value to detect change. For optimization only.
     */
    private Set<String> oldAllowedOrigins;

    @Override
    public void init(final FilterConfig config) throws ServletException {
        this.config = config;

        this.enabled = (Boolean) getBackendParameter(ConfigValues.CORSSupport);
        this.enabledDefaultOrigins = (Boolean) getBackendParameter(ConfigValues.CORSAllowDefaultOrigins);
        String sufficesFromConf = StringUtils.defaultString(
                (String) getBackendParameter(ConfigValues.CORSDefaultOriginSuffixes), "");
        this.defaultOriginsSuffixes = new HashSet<>(Arrays.asList(sufficesFromConf.split(",")));
    }

    @Override
    public void destroy() {
        if (delegate != null) {
            delegate.destroy();
        }
    }

    private void createDelegate() throws ServletException {
        // Check if the CORS support is enabled in configuration:
        if (enabled == null || !enabled) {
            log.info("CORS support is disabled.");
            return;
        }

        // Get the allowed origins from the backend configuration:
        final String allowedOriginsConfig = (String) getBackendParameter(ConfigValues.CORSAllowedOrigins);
        final Set<String> allowedDefaultOrigins = getDefaultAllowedOrigins();
        final String allowedOrigins = mergeOrigins(allowedOriginsConfig, allowedDefaultOrigins);
        if (StringUtils.isEmpty(allowedOrigins)) {
            log.warn(
                "The CORS support has been enabled, but the list of allowed origins is empty. This means that CORS " +
                "support will actually be disabled."
            );
            return;
        }
        log.info("CORS support is enabled for origins \"{}\".", allowedOrigins);

        if (delegate == null || !allowedDefaultOrigins.equals(oldAllowedOrigins)) {
            // Create new CORSFilter() only if needed
            oldAllowedOrigins = allowedDefaultOrigins;

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
    }

    private String mergeOrigins(String fromConfig, Set<String> fromDefault) {
        if ("*".equals(fromConfig)) {
            return fromConfig;
        }
        if (StringUtils.isEmpty(fromConfig)) {
            return StringUtils.join(fromDefault, ',');
        }
        return fromConfig + "," + StringUtils.join(fromDefault, ',');
    }

    private Object getBackendParameter(final ConfigValues key) throws ServletException {
        final GetConfigurationValueParameters parameters = new GetConfigurationValueParameters();
        parameters.setConfigValue(key);
        parameters.setVersion(ConfigCommon.defaultConfigurationVersion);
        QueryReturnValue value = backend.runPublicQuery(QueryType.GetConfigurationValue, parameters);
        if (!value.getSucceeded()) {
            throw new ServletException("Can't get value of backend parameter \"" + key + "\".");
        }
        return value.getReturnValue();
    }

    private Set<String> getDefaultAllowedOrigins() throws ServletException {
        if (this.enabledDefaultOrigins) {
            GetDefaultAllowedOriginsQueryParameters parameters = new GetDefaultAllowedOriginsQueryParameters();
            parameters.addSuffixes(defaultOriginsSuffixes);
            QueryReturnValue value = backend.runPublicQuery(QueryType.GetDefaultAllowedOrigins, parameters);
            if (!value.getSucceeded()) {
                throw new ServletException("Can't get list of default origins");
            }
            if (log.isDebugEnabled()) {
                log.debug("Origins allowed by default: {}",
                        StringUtils.join((Set<String>) value.getReturnValue(), ','));
            }
            return value.getReturnValue();
        }
        return EMPTY_SET;
    }

    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        // Result of GetDefaultAllowedOriginsQuery might vary in time, so
        // reinitialize if needed - new CORSFilter needs to be created with new params
        if (delegate != null) { // is CORSSupport enabled by engine-config ?
            if (initialized) { // for performance reasons, check for changes at most once per time period
                long now = System.currentTimeMillis();
                if (lastInitializationTime + delayBeforeReinit < now) {
                    synchronized (this) {
                        initialized = false; // force reinitialization
                    }
                    lastInitializationTime = now;
                }
            }
        }

        // Perform lazy initialization, if needed:
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    createDelegate();
                    initialized = true;
                }
            }
        }

        // Do the filtering if needed:
        if (delegate != null) {
            delegate.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }
}
