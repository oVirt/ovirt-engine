/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.doc;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.api.restapi.LocalConfig;
import org.ovirt.engine.core.utils.servlet.ServletUtils;

/**
 * This filter manages the API documentation. If the API explorer application is installed then it will redirect the
 * requests to that application, otherwise it will deliver the static HTML documentation.
 */
public class DocFilter implements Filter {
    // The directory where the explorer application is installed. Will be "null" if the application isn't installed.
    private File dir;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Get the directory of the explorer application:
        String path = LocalConfig.getInstance().getExplorerDirectory();
        if (path != null && !path.isEmpty()) {
            dir = new File(path);
        }
    }

    @Override
    public void destroy() {
        // Nothing.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Just cast request and response types so that the method that does the real work is easier to read.
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Get the relative path of the requested file:
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.isEmpty()) {
            path = "/";
        }

        // Send the file if it exists within the explorer application:
        File file = getExplorerFile(path);
        if (file != null) {
            ServletUtils.sendFile(request, response, file, null);
            return;
        }

        // If the does not exist within the explorer application then use the default servlet, which will work fine
        // for the content of this application, like "model.json", and will generate errors for things that don't exist
        // in the explorer nor in this application.
        chain.doFilter(request, response);
    }

    /**
     * Returns the file object corresponding to the given path inside the explorer application. If the application isn't
     * installed, or it doesn't contain such file, it will return {@code null}.
     *
     * @param path the path of the file inside the explorer application
     * @return the file object, or {@code null} if no such file exists in the explorer application
     */
    private File getExplorerFile(String path) {
        // No file if the explorer directory doesn't exist:
        if (dir == null) {
            return null;
        }

        // Check if this is a request for the index page:
        if (path.equals("/")) {
            path += "index.html";
        }

        // Make sure that the path is sane:
        File file = ServletUtils.makeFileFromSanePath(path, dir);
        if (file == null) {
            return null;
        }

        // Make sure that the file exits:
        if (!file.exists()) {
            return null;
        }

        return file;
    }
}
