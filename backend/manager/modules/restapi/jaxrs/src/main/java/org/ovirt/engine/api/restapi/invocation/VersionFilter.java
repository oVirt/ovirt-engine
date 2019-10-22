/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.invocation;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.api.restapi.DeprecatedVersionInfo;
import org.ovirt.engine.api.restapi.LocalConfig;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDeprecatedApiEventParameters;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(VersionFilter.class);

    // Regular expression used to start the version number from the request path:
    private static final String VERSION_GROUP = "version";
    private static final String PATH_GROUP = "path";
    private static final Pattern VERSION_PATTERN = Pattern.compile(
        "^/v(?<" + VERSION_GROUP + ">[0-9]+)(?<" + PATH_GROUP + ">(/.*))?$"
    );

    // Names of headers:
    private static final String VERSION_HEADER = "Version";

    // The reference to the backend bean:
    @SuppressWarnings("unused")
    @EJB(lookup = "java:global/engine/bll/Backend!org.ovirt.engine.core.common.interfaces.BackendLocal")
    private BackendLocal backend;

    // Supported and default versions:
    private Set<String> supportedVersions;
    private String defaultVersion;

    // Deprecated versions:
    private Set<String> deprecatedVersionsSet;
    private Map<String, DeprecatedVersionInfo> deprecatedVersionsMap;

    @Override
    public void init(FilterConfig config) throws ServletException {
        LocalConfig localConfig = LocalConfig.getInstance();

        // Get the supported and default versions:
        supportedVersions = localConfig.getSupportedVersions();
        defaultVersion = localConfig.getDefaultVersion();

        // Get the information about deprecated versions and store them in a set and a map for easy/fast access:
        Set<DeprecatedVersionInfo> deprecatedVersionInfos = localConfig.getDeprecatedVersions();
        deprecatedVersionsSet = deprecatedVersionInfos.stream()
            .map(DeprecatedVersionInfo::getVersion)
            .collect(toSet());
        deprecatedVersionsMap = deprecatedVersionInfos.stream()
            .collect(toMap(DeprecatedVersionInfo::getVersion, identity()));
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
        // Get a reference to the object that stores the information of the current request:
        Current current = CurrentManager.get();

        // Get the remote address, as we need it for several things:
        String remoteAddress = request.getRemoteAddr();

        // First try to extract the version from the request path:
        String version = null;
        VersionSource source = null;
        String path = current.getPath();
        Matcher matcher = VERSION_PATTERN.matcher(path);
        if (matcher.matches()) {
            version = matcher.group(VERSION_GROUP);
            path = matcher.group(PATH_GROUP);
            source = VersionSource.URL;
        }

        // If the version hasn't been determined yet, then try to extract it from the headers:
        if (version == null || version.isEmpty()) {
            version = request.getHeader(VERSION_HEADER);
            if (version != null && !version.isEmpty()) {
                source = VersionSource.HEADER;
            }
        }

        // Finally, if the version hasn't been determined, then use the default:
        if (version == null || version.isEmpty()) {
            version = defaultVersion;
            source = VersionSource.DEFAULT;
        }

        // Check that the version is supported, and return an HTTP error response if it isn't:
        if (!supportedVersions.contains(version)) {
            log.error(
                "Client \"{}\" is requesting unsupported version \"{}\", will send a 400 error code.",
                remoteAddress, version
            );
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Check if the version is deprecated, if it is then send a message to the audit log:
        if (deprecatedVersionsSet.contains(version)) {
            DeprecatedVersionInfo versionInfo = deprecatedVersionsMap.get(version);
            AddDeprecatedApiEventParameters parameters = new AddDeprecatedApiEventParameters(version, remoteAddress,
                versionInfo.getDeprecating(), versionInfo.getRemoving());
            backend.runAction(ActionType.AddDeprecatedApiEvent, parameters);
        }

        // Copy the version, the source and the path to the object that stores information to the current request:
        current.setVersion(version);
        current.setVersionSource(source);
        current.setPath(path);

        // If the version was extracted from the URL then we can pass the request directly to the next filter of the
        // chain. Otherwise we need to modify the path, adding the version prefix, and then we need to forward the
        // modified request.
        if (source == VersionSource.URL) {
            chain.doFilter(request, response);
        } else {
            String prefix = current.getPrefix();
            String uri = request.getRequestURI();
            StringBuilder buffer = new StringBuilder(2 + version.length() + (uri.length() - prefix.length()));
            buffer.append("/v");
            buffer.append(version);
            buffer.append(uri, prefix.length(), uri.length());
            path = buffer.toString();
            RequestDispatcher dispatcher = request.getRequestDispatcher(path);
            if (dispatcher == null) {
                log.error(
                    "Can't find dispatcher for path \"{}\", as requested by client \"{}\", will send a 404 error code.",
                    path, remoteAddress
                );
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                dispatcher.forward(request, response);
            }
        }
    }
}
