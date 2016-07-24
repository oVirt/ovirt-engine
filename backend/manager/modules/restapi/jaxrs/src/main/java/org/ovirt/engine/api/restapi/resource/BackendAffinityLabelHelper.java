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
package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

/**
 * This class contains a collection of methods using when working with affinity labels.
 */
public class BackendAffinityLabelHelper {
    /**
     * Retrieves a backend affinity label given its identifier.
     *
     * @param id the identifier of the label
     * @return  returns the requested label if it exists, or throws a {@code 404} exception if it doesn't exist
     */
    public static Label getLabel(BackendResource resource, String id) {
        IdQueryParameters parameters = new IdQueryParameters(GuidUtils.asGuid(id));
        return resource.getEntity(
            Label.class,
            VdcQueryType.GetLabelById,
            parameters,
            id,
            true
        );
    }

    /**
     * Creates a link to a virtual machine.
     *
     * @param id the identifier of the virtual machine
     * @return a {@code Vm} object containing only the {@code id} and {@code href} attributes
     */
    public static Vm makeVmLink(Guid id) {
        Vm vm = new Vm();
        vm.setId(id.toString());
        LinkHelper.addLinks(vm, null, false);
        return vm;
    }

    /**
     * Creates a link to a host.
     *
     * @param id the identifier of the host
     * @return a {@code Host} object containing only the {@code id} and {@code href} attributes
     */
    public static Host makeHostLink(Guid id) {
        Host host = new Host();
        host.setId(id.toString());
        LinkHelper.addLinks(host, null, false);
        return host;
    }
}
