/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi;

import javax.servlet.http.HttpServlet;

/**
 * This empty servlet will never be actually called, but it is needed because we need to map something to the root
 * of the application. For example, if the context root is {@code /ovirt-engine/api}, without a trailing slash, and
 * the user sends a request like {@code GET /ovirt-engine/api} then the servlet container will assume that the request
 * corresponds to a directory, and will send a {@code 302} response with a {@code Location} header containing the same
 * path plus the trailing slash. The request will look like this:
 *
 * <pre>
 * GET /ovirt-engine/api HTTP/1.1
 * </pre>
 *
 * And the response will look like this:
 *
 * <pre>
 * HTTP/1.1 302 Found
 * Location: https://engine.example.com/ovirt-engine/api/
 * </pre>
 *
 * This redirect works fine for sophisticated clients, like browsers, but simpler clients, like the Python and Java
 * SDKs don't support it.
 *
 * Mapping this servlet to the root of the application (using {@code <url-pattern>/*</url-pattern>} solves this problem.
 *
 * Note that the servlet will never be called, because the version filter will always forward this kind of requests
 * to {@code /v3} or {@code /v4}.
 */
public class NullServlet extends HttpServlet {
}
