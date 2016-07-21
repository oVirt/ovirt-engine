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

package org.ovirt.engine.api.restapi.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
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

/**
 * A container of static methods related to query resolution.
 */
public class QueryHelper {
    public static final String CONSTRAINT_PARAMETER = "search";

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
     * @param headers the HTTP headers to extract the query from
     * @param ui the URI to extract the query from
     * @param clz the individual return type expected from the query
     * @return constraint in correct format
     */
    public static String getConstraint(HttpHeaders headers, UriInfo ui, Class<?> clz) {
        return getConstraint(headers, ui, null, clz);
    }

    /**
     * Extract constraint from query parameters.
     *
     * @param ui the URI to extract the parameters from
     * @param clz the individual return type expected from the query
     * @param typePrefix true if return type prefix is to be included
     * @return constraint in correct format
     */
    public static String getConstraint(HttpHeaders headers, UriInfo ui, Class<?> clz, boolean typePrefix) {
        return getConstraint(headers, ui, null, clz, typePrefix);
    }

    /**
     * Extract constraint from query parameters.
     *
     * @param headers the HTTP headers to extract the search query from
     * @param ui the URI to extract the search query from
     * @param defaultQuery raw query to use if not present in URI parameters
     * @param clz the individual return type expected from the query
     * @return constraint in correct format
     */
    public static String getConstraint(HttpHeaders headers, UriInfo ui, String defaultQuery, Class<?> clz) {
        return getConstraint(headers, ui, defaultQuery, clz, true);
    }

    /**
     * Extract constraint from query parameters.
     *
     * @param headers the HTTP headers to extract the search query from
     * @param ui the URI to extract the search query from
     * @param defaultQuery raw query to use if not present in URI parameters
     * @param clz the individual return type expected from the query
     * @param typePrefix true if return type prefix is to be included
     * @return constraint in correct format
     */
    public static String getConstraint(HttpHeaders headers, UriInfo ui, String defaultQuery, Class<?> clz, boolean typePrefix) {
        String prefix = typePrefix? RETURN_TYPES.get(clz) : "";
        String search = ParametersHelper.getParameter(headers, ui, CONSTRAINT_PARAMETER);
        if (search != null) {
            return prefix + search;
        }
        if (defaultQuery != null) {
            return prefix + defaultQuery;
        }
        return null;
    }
}
