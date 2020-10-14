/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.restapi.resource.BackendResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a helper that extracts parameters from HTTP headers, matrix parameters and query parameters.
 */
public class ParametersHelper {
    private static final Logger log = LoggerFactory.getLogger(ParametersHelper.class);

    // Regular expressions for boolean values:
    private static final Pattern FALSE_PATTERN = Pattern.compile("^(f(alse)?|n(o)?|0)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRUE_PATTERN = Pattern.compile("^(t(rue)?|y(es)?|1)$", Pattern.CASE_INSENSITIVE);

    /**
     * Some parameters must also be allowed in headers, for backwards compatibility mostly.
     */
    private static final Set<String> HEADER_PARAMETERS = new HashSet<>();

    static {
        HEADER_PARAMETERS.add("filter");
        HEADER_PARAMETERS.add(BackendResource.ALL_CONTENT_HEADER);
    }

    private ParametersHelper() {
        // No instances allowed.
    }

    /**
     * Gets the value of a parameter, from a header, matrix parameter or query parameter.
     *
     * @param headers the object that gives access to the HTTP headers of the request, may be {@code null} in which case
     *     it will be completely ignored
     * @param uri the object that gives access to the URI information, may be {@code null} in which case it will be
     *     completely ignored
     * @param name the name of the parameter
     * @return the value of the parameter, may be empty, or {@code null} if there is no such parameter in the request
     */
    public static String getParameter(HttpHeaders headers, UriInfo uri, String name) {
        // Check if there is a request parameter providing the value:
        Current current = CurrentManager.get();
        if (current != null) {
            Map<String, String> parameters = current.getParameters();
            if (MapUtils.isNotEmpty(parameters)) {
                if (parameters.containsKey(name)) {
                    String value = parameters.get(name);
                    if (value == null) {
                        value = "";
                    }
                    return value;
                }
            }
        }

        // Check if there is a query parameter providing the value:
        if (uri != null) {
            MultivaluedMap<String, String> parameters = uri.getQueryParameters();
            if (MapUtils.isNotEmpty(parameters)) {
                if (parameters.containsKey(name)) {
                    String value = parameters.getFirst(name);
                    if (value == null) {
                        value = "";
                    }
                    return value;
                }
            }
        }

        // Check if there is a matrix parameter providing the value:
        if (uri != null) {
            List<PathSegment> segments = uri.getPathSegments();
            if (CollectionUtils.isNotEmpty(segments)) {
                PathSegment last = segments.get(segments.size() - 1);
                if (last != null) {
                    MultivaluedMap<String, String> parameters = last.getMatrixParameters();
                    if (MapUtils.isNotEmpty(parameters)) {
                        if (parameters.containsKey(name)) {
                            String value = parameters.getFirst(name);
                            if (value == null) {
                                value = "";
                            }
                            return value;
                        }
                    }
                }
            }
        }

        // Check if there is a header providing the value:
        if (headers != null && HEADER_PARAMETERS.contains(name)) {
            List<String> values = headers.getRequestHeader(name);
            if (CollectionUtils.isNotEmpty(values)) {
                String value = values.get(0);
                if (value != null) {
                    return value;
                }
            }
        }

        // No luck:
        return null;
    }

    /**
     * Returns the boolean value of the given parameter. If the parameter is present in the request but it doesn't have
     * a value then the value of the {@code empty} parameter will be returned. If the parameter isn't present, or has an
     * invalid boolean value then the value of the {@code missing} parameter will be returned.
     *
     * @param headers the HTTP headers to extract the parameter from
     * @param uri the URL to extract the parameter from
     * @param name the name of the parameter
     * @param empty the value that will be returned if the parameter is present but has no value
     * @param missing the value that will be returned if the parameter isn't present or has an invalid boolean value
     */
    public static Boolean getBooleanParameter(HttpHeaders headers, UriInfo uri, String name, Boolean empty,
            Boolean missing) {
        String text = getParameter(headers, uri, name);
        if (text == null) {
            return missing;
        }
        if (text.isEmpty()) {
            return empty;
        }
        if (FALSE_PATTERN.matcher(text).matches()) {
            return Boolean.FALSE;
        }
        if (TRUE_PATTERN.matcher(text).matches()) {
            return Boolean.TRUE;
        }
        log.error("The value \"{}\" of parameter \"{}\" isn't a valid boolean, it will be ignored.", text, name);
        return missing;
    }

    /**
     * Returns the integer value of the given parameter. If the parameter is present in the request but it doesn't have
     * a value then the value of the {@code empty} parameter will be returned. If the matrix parameter isn't present, or
     * has an invalid integer value then the value of the {@code missing} parameter will be returned.
     *
     * @param headers the HTTP headers to extract the parameter from
     * @param uri the URL to extract the parameter from
     * @param name the name of the parameter
     * @param empty the value that will be returned if the parameter is present but has no value
     * @param missing the value that will be returned if the parameter isn't present or has in invalid integer value
     */
    public static int getIntegerParameter(HttpHeaders headers, UriInfo uri, String name, int empty, int missing) {
        String text = getParameter(headers, uri, name);
        if (text == null) {
            return missing;
        }
        if (text.isEmpty()) {
            return empty;
        }
        try {
            return Integer.parseInt(text);
        } catch(NumberFormatException exception) {
            log.error("The value \"{}\" of parameter \"{}\" isn't a valid integer, it will be ignored.", text, name);
            return missing;
        }
    }

    /**
     * Set a parameter in the parameters map.
     */
    public static void setParameter(String name, String value) {
        Current current = CurrentManager.get();
        if (current != null) {
            Map<String, String> parameters = current.getParameters();
            parameters.put(name, value);
        }
    }

    /**
     * Remove a parameter from the parameters map.
     * This is done by setting "" as the value. This is because
     * when searching, if a parameter is missing, it is fetched
     * directly from he URL, and if we explicitly declare that
     * we wish to remove a parameter - we would like to avoid
     * searching in the URL.
     */
    public static void removeParameter(String name) {
        setParameter(name, "");
    }
}
