/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.common.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.core.common.utils.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A container of static methods related to query resolution.
 */
public class QueryHelper {
    private static final Logger log = LoggerFactory.getLogger(QueryHelper.class);

    public static final String CONSTRAINT_PARAMETER = "search";

    public static final String CURRENT_CONSTRAINT_PARAMETER = "current";

    private QueryHelper() {}

    /**
     * Map return types per-collection-class, as there's no logical pattern
     * REVISIT: can we safely just drop the return type specifier?
     * (doesn't seem to have any effect in the powershell case)
     * REVISIT: RHEVM Admin Guide is not very clear on whether these
     * return type specifiers should always be pluralized
     */
    private static final Map<Class<?>, String> RETURN_TYPES = createReturnTypes();

    private static Map<Class<?>, String> createReturnTypes() {
        final Map<Class<?>, String> map = new HashMap<>();

        map.put(Vm.class, getName("VMs"));
        map.put(Host.class, getName("Hosts"));
        map.put(Cluster.class, getName("Clusters"));
        map.put(DataCenter.class, getName("Datacenter"));
        map.put(StorageDomain.class, getName("Storage"));
        map.put(Template.class, getName("Template"));
        map.put(InstanceType.class, getName("Instancetypes"));
        map.put(User.class, getName("Users"));
        map.put(Group.class, getName("Groups"));
        map.put(VmPool.class, getName("Pools"));
        map.put(Event.class, getName("Events"));
        map.put(GlusterVolume.class, getName("Volumes"));
        map.put(Disk.class, getName("Disks"));
        map.put(Network.class, getName("Networks"));

        return Collections.unmodifiableMap(map);
    }

    private static String getName(String name) {
        return name + CommonConstants.QUERY_RETURN_TYPE_SEPARATOR;
    }

    /**
     * Extract constraint from query parameters.
     *
     * @param uriInfo  contains query parameters if set
     * @param clz      the individual return type expected from the query
     * @return         constraint in correct format
     */
    public static String getConstraint(UriInfo uriInfo, Class<?> clz) {
        return getConstraint(uriInfo, null, clz);
    }

    /**
     * Extract constraint from query parameters.
     *
     * @param uriInfo  contains query parameters if set
     * @param clz      the individual return type expected from the query
     * @param typePrefix    true if return type prefix is to be included
     * @return         constraint in correct format
     */
    public static String getConstraint(UriInfo uriInfo, Class<?> clz, boolean typePrefix) {
        return getConstraint(uriInfo, null, clz, typePrefix);
    }

    /**
     * Extract constraint from query parameters.
     *
     * @param uriInfo       contains query parameters if set
     * @param defaultQuery  raw query to use if not present in URI parameters
     * @param clz           the individual return type expected from the query
     * @return              constraint in correct format
     */
    public static String getConstraint(UriInfo uriInfo, String defaultQuery, Class<?> clz) {
        return getConstraint(uriInfo, defaultQuery, clz, true);
    }

    /**
     * Extract constraint from query parameters.
     *
     * @param uriInfo       contains query parameters if set
     * @param defaultQuery  raw query to use if not present in URI parameters
     * @param clz           the individual return type expected from the query
     * @param typePrefix    true if return type prefix is to be included
     * @return              constraint in correct format
     */
    public static String getConstraint(UriInfo uriInfo, String defaultQuery, Class<?> clz, boolean typePrefix) {
        String prefix = typePrefix ? RETURN_TYPES.get(clz) : "";
        HashMap<String, String> constraints = getQueryConstraints(uriInfo, CONSTRAINT_PARAMETER);

        return constraints != null && constraints.containsKey(CONSTRAINT_PARAMETER)
               ? prefix + constraints.get(CONSTRAINT_PARAMETER)
               : defaultQuery != null
                 ? prefix + defaultQuery
                 : null;
    }

    public static String getConstraint(MultivaluedMap<String, String> queries, String constraint) {
        return queries != null
               && queries.get(constraint) != null
               && queries.get(constraint).size() > 0 ? queries.get(constraint).get(0)
                                                       :
                                                       null;
    }

    public static boolean hasConstraint(MultivaluedMap<String, String> queries, String constraint) {
        return queries != null && queries.containsKey(constraint) ? true : false;
    }

    public static boolean hasConstraint(UriInfo uriInfo, String constraint) {
            return hasConstraint(uriInfo.getQueryParameters(), constraint);
    }

    public static boolean hasMatrixParam(UriInfo uriInfo, String param) {
        return hasMatrixParam(uriInfo.getPathSegments(), param);
    }

    private static boolean hasMatrixParam(List<PathSegment> pathSegments, String param) {
        if (pathSegments != null)
            for (PathSegment segement : pathSegments) {
                MultivaluedMap<String, String> matrixParams = segement.getMatrixParameters();
                if (matrixParams != null && !matrixParams.isEmpty() && matrixParams.containsKey(param))
                    return true;
            }
        return false;
    }

    public static HashMap<String, String> getQueryConstraints(UriInfo uriInfo, String... constraints) {
        HashMap<String, String> params = new HashMap<String, String>();
        if (constraints != null && constraints.length > 0) {
            for (String key : constraints) {
                String value = getConstraint(uriInfo.getQueryParameters(), key);
                if (value != null && !value.isEmpty()) {
                    params.put(key, value);
                }
            }
        }
        return params;
    }

    public static HashMap<String, String> getMatrixConstraints(UriInfo uriInfo, String... constraints) {
        HashMap<String, String> params = new HashMap<String, String>();
        if (uriInfo.getPathSegments() != null && constraints != null && constraints.length > 0) {
            for (String key : constraints) {
                for (PathSegment segement : uriInfo.getPathSegments()) {
                    MultivaluedMap<String, String> matrixParams = segement.getMatrixParameters();
                    if (matrixParams != null && !matrixParams.isEmpty() && matrixParams.containsKey(key))
                        params.put(key, getConstraint(matrixParams, key));
                }
            }
        }
        return params;
    }

    public static String getMatrixConstraint(UriInfo uriInfo, String constraint) {
        HashMap<String, String> constraints = getMatrixConstraints(uriInfo, constraint);
        return constraints.containsKey(constraint) ? constraints.get(constraint) : null;
    }

    /**
     * Returns the boolean value of the given matrix parameter. If the matrix parameter is present in the request but it
     * doesn't have a value then the value of the {@code empty} parameter will be returned. If the matrix parameter
     * isn't present, or has an invalid boolean value then the value of the {@code missing} parameter will be returned.
     *
     * @param uri the URL to extract the parameter from
     * @param name the name of the parameter
     * @param empty the value that will be returned if the parameter is present but has no value
     * @param missing the value that will be returned if the parameter isn't present or has an invalid boolean value
     */
    public static boolean getBooleanMatrixParameter(UriInfo uri, String name, boolean empty, boolean missing) {
        String text = getMatrixConstraint(uri, name);
        if (text == null) {
            return missing;
        }
        if (text.isEmpty()) {
            return empty;
        }
        switch (text) {
        case "true":
            return true;
        case "false":
            return false;
        default:
            log.error(
                "The value \"{}\" of matrix parameter \"{}\" isn't a valid boolean, it will be ignored.",
                text, name
            );
            return missing;
        }
    }

    /**
     * Returns the integer value of the given matrix parameter. If the matrix parameter is present in the request but it
     * doesn't have a value then the value of the {@code empty} parameter will be returned. If the matrix parameter
     * isn't present, or has an invalid integer value then the value of the {@code missing} parameter will be returned.
     *
     * @param uri the URL to extract the parameter from
     * @param name the name of the parameter
     * @param empty the value that will be returned if the parameter is present but has no value
     * @param missing the value that will be returned if the parameter isn't present or has in invalid integer value
     */
    public static int getIntegerMatrixParameter(UriInfo uri, String name, int empty, int missing) {
        String text = getMatrixConstraint(uri, name);
        if (text == null) {
            return missing;
        }
        if (text.isEmpty()) {
            return empty;
        }
        try {
            return Integer.parseInt(text);
        }
        catch (NumberFormatException exception) {
            log.error(
                "The value \"{}\" of matrix parameter \"{}\" isn't a valid integer, it will be ignored.",
                text, name
            );
            log.error("Exception", exception);
            return missing;
        }
    }

    public static boolean hasCurrentConstraint(UriInfo uriInfo) {
        // TODO: CURRENT_CONSTRAINT_PARAMETER as query parameter is depreciated and supported
        // for backward compatibility only - should be dropped at 4.0.
        return QueryHelper.hasConstraint(uriInfo.getQueryParameters(), CURRENT_CONSTRAINT_PARAMETER) ||
                (hasMatrixParam(uriInfo, CURRENT_CONSTRAINT_PARAMETER) &&
                 !"false".equalsIgnoreCase(QueryHelper.getMatrixConstraint(uriInfo, CURRENT_CONSTRAINT_PARAMETER)));
    }
}
