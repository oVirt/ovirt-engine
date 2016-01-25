/*
Copyright (c) 2016 Red Hat, Inc.

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VersionFilter implements Filter {
    // Regular expression used to start the version number from the request path:
    private static final String VERSION_GROUP = "version";
    private static final String PATH_GROUP = "path";
    private static final Pattern VERSION_PATTERN = Pattern.compile(
        "^/v(?<" + VERSION_GROUP + ">[0-9]+)(?<" + PATH_GROUP + ">(/.*))?$"
    );

    // Names of headers:
    private static final String VERSION_HEADER = "Version";

    // Default values of headers (in the future this will be a configuration parameter):
    private static final String VERSION_DEFAULT = "4";

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
        // Get a reference to the object that stores the information of the current request:
        Current current = CurrentManager.get();

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
            version = VERSION_DEFAULT;
            source = VersionSource.DEFAULT;
        }

        // Copy the version, the source and the path to the object that stores information to the current request:
        current.setVersion(version);
        current.setVersionSource(source);
        current.setPath(path);

        // If the version was extracted from the URL then we can pass the request directly to the next filter of the
        // chain. Otherwise we need to modify the path, adding the version prefix, and then we need to forward the
        // modified request.
        switch (source) {
        case URL:
            chain.doFilter(request, response);
            break;
        default:
            request.getRequestDispatcher("/v" + version + path).forward(request, response);
        }
    }
}
