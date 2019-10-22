/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeaderValueParser;

/**
 * This class is responsible for determining what details should be included in a response. The details can be specified
 * using the {@code detail} parameter of the HTTP {@code Accept} header, or with the {@code detail} matrix or query
 * parameter. The value of this parameter should a list of names preceded by the plus or minus signs. If the name is
 * preceded by the an plus sign (or not preceded by any sign, only for the first name)then it will be included,
 * otherwise it will be included. For example, to request the information of NICs and disks using the header:
 *
 * <pre>
 * GET /vms/{vm:id} HTTP/1.1
 * Accept: application/xml; detail=nics+disks
 * </pre>
 *
 * Same using a matrix parameter (this is the preferred way, as proxy servers may then cache the modified content):
 *
 * <pre>
 * GET /vms/{vm:id};detail=nics+disks HTTP/1.1
 * </pre>
 *
 * Same using a query parameter:
 *
 * <pre>
 * GET /vms/{vm:id}?detail=nics+disks HTTP/1.1
 * </pre>
 *
 * The minus sing is used to exclude some detail that is included by default. For example, it could be used to
 * specify that only the size of a collection is requested, but not its actual data:
 *
 * <pre>
 * GET /vms;detail=-main+size HTTP/1.1
 * </pre>
 *
 * When not specified otherwise the main data will by default be included.
 */
public class DetailHelper {
    /**
     * The name of the HTTP {@code Accept} header.
     */
    private static final String ACCEPT = "Accept";

    /**
     * The name of the header, matrix, or query parameter that contains the list of details to include or exclude.
     */
    private static final String DETAIL = "detail";

    /**
     * The name of the detail name that indicates that the main data should be included.
     */
    public static final String MAIN = "main";

    /**
     * Determines what details to include or exclude from the {@code detail} parameter of the {@code Accept} header and
     * from the {@code detail} matrix or query parameters.
     *
     * @param headers the object that gives access to the HTTP headers of the request, may be {@code null} in which case
     *     it will be completely ignored
     * @param uri the object that gives access to the URI information, may be {@code null} in which case it will be
     *     completely ignored
     * @return the set containing the extracted information, may be empty, but never {@code null}
     */
    public static Set<String> getDetails(HttpHeaders headers, UriInfo uri) {
        // We will collect the detail specifications obtained from different places into this list, for later
        // processing:
        List<String> allSpecs = new ArrayList<>(0);

        // Try to extract the specification of what to include/exclude from the accept header:
        if (headers != null) {
            List<String> headerValues = headers.getRequestHeader(ACCEPT);
            if (CollectionUtils.isNotEmpty(headerValues)) {
                for (String headerValue : headerValues) {
                    HeaderElement[] headerElements = BasicHeaderValueParser.parseElements(headerValue, null);
                    if (ArrayUtils.isNotEmpty(headerElements)) {
                        for (HeaderElement headerElement : headerElements) {
                            for (NameValuePair parameter : headerElement.getParameters()) {
                                if (StringUtils.equalsIgnoreCase(parameter.getName(), DETAIL)) {
                                    String spec = parameter.getValue();
                                    if (StringUtils.isNotEmpty(spec)) {
                                        allSpecs.add(parameter.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Try also from the matrix parameters:
        if (uri != null) {
            List<PathSegment> segments = uri.getPathSegments();
            if (CollectionUtils.isNotEmpty(segments)) {
                PathSegment last = segments.get(segments.size() - 1);
                if (last != null) {
                    MultivaluedMap<String, String> parameters = last.getMatrixParameters();
                    if (MapUtils.isNotEmpty(parameters)) {
                        List<String> specs = parameters.get(DETAIL);
                        if (CollectionUtils.isNotEmpty(specs)) {
                            allSpecs.addAll(specs);
                        }
                    }
                }
            }
        }

        // Try also from the query parameters:
        if (uri != null) {
            MultivaluedMap<String, String> parameters = uri.getQueryParameters();
            if (MapUtils.isNotEmpty(parameters)) {
                List<String> specs = parameters.get(DETAIL);
                if (CollectionUtils.isNotEmpty(specs)) {
                    allSpecs.addAll(specs);
                }
            }
        }

        // Process all the obtained detail specifications:
        return parseDetails(allSpecs);
    }

    /**
     * Parses a string into the object that represents what to include.
     *
     * @param specs the specification of what to include or exclude
     * @return the set that represents what to include, which may be completely empty, but never
     *     {@code null}
     */
    private static Set<String> parseDetails(List<String> specs) {
        // In most cases the user won't give any detail specification, so it is worth to avoid creating an expensive
        // set in that case:
        if (CollectionUtils.isEmpty(specs)) {
            return Collections.singleton(MAIN);
        }

        // If the user gave a detail specification then we need first to add the default value and then parse it:
        Set<String> details = new HashSet<>(2);
        details.add(MAIN);
        if (CollectionUtils.isNotEmpty(specs)) {
            for (String spec : specs) {
                if (spec != null) {
                    String[] chunks = spec.split("(?=[+-])");
                    if (ArrayUtils.isNotEmpty(chunks)) {
                        for (String chunk : chunks) {
                            chunk = chunk.trim();
                            if (chunk.startsWith("+")) {
                                chunk = chunk.substring(1).trim();
                                if (StringUtils.isNotEmpty(chunk)) {
                                    details.add(chunk);
                                }
                            } else if (chunk.startsWith("-")) {
                                chunk = chunk.substring(1).trim();
                                if (StringUtils.isNotEmpty(chunk)) {
                                    details.remove(chunk);
                                }
                            } else {
                                details.add(chunk);
                            }
                        }
                    }
                }
            }
        }
        return details;
    }
}
