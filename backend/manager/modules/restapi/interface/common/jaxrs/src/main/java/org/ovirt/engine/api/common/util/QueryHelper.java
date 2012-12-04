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
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VmPool;

/**
 * A container of static methods related to query resolution.
 */
public class QueryHelper {

    public static final String CONSTRAINT_PARAMETER = "search";
    private static final String RETURN_TYPE_SEPARTOR = " : ";

    private QueryHelper() {}

    /**
     * Map return types per-collection-class, as there's no logical pattern
     * REVISIT: can we safely just drop the return type specifier?
     * (doesn't seem to have any effect in the powershell case)
     */
    public static Map<Class<?>, String> RETURN_TYPES;

    static {
        RETURN_TYPES = new HashMap<Class<?>, String>();
        /**
         * REVISIT: RHEVM Admin Guide is not very clear on whether these
         * return type specifiers should always be pluralized
         */
        RETURN_TYPES.put(VM.class, "VMs" + RETURN_TYPE_SEPARTOR);
        RETURN_TYPES.put(Host.class, "Hosts" + RETURN_TYPE_SEPARTOR);
        RETURN_TYPES.put(Cluster.class, "Clusters" + RETURN_TYPE_SEPARTOR);
        RETURN_TYPES.put(DataCenter.class, "Datacenter" + RETURN_TYPE_SEPARTOR);
        RETURN_TYPES.put(StorageDomain.class, "Storage" + RETURN_TYPE_SEPARTOR);
        RETURN_TYPES.put(Template.class, "Template" + RETURN_TYPE_SEPARTOR);
        RETURN_TYPES.put(User.class, "Users" + RETURN_TYPE_SEPARTOR);
        RETURN_TYPES.put(VmPool.class, "Pools" + RETURN_TYPE_SEPARTOR);
        RETURN_TYPES.put(Event.class, "Events" + RETURN_TYPE_SEPARTOR);
        RETURN_TYPES.put(GlusterVolume.class, "Volumes" + RETURN_TYPE_SEPARTOR);
        RETURN_TYPES.put(Disk.class, "Disks" + RETURN_TYPE_SEPARTOR);
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
}
