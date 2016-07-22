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

package org.ovirt.engine.api.v3.servers;

import java.util.Map;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.resource.VmsResource;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.helpers.V3VmHelper;
import org.ovirt.engine.api.v3.types.V3Disks;
import org.ovirt.engine.api.v3.types.V3Permissions;
import org.ovirt.engine.api.v3.types.V3VM;
import org.ovirt.engine.api.v3.types.V3VMs;

@Produces({"application/xml", "application/json"})
public class V3VmsServer extends V3Server<VmsResource> {
    public V3VmsServer(VmsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(@Context UriInfo ui, V3VM vm) {
        Map<String, String> parameters = CurrentManager.get().getParameters();

        // V3 version of the API used the "clone" element of the disks as a parameter, but in V4 this has been replaced
        // with equivalent parameter:
        if (vm.isSetDisks()) {
            V3Disks disks = vm.getDisks();
            if (disks.isSetClone() && disks.isClone()) {
                parameters.put("clone", String.valueOf(true));
            }
        }

        // V3 version of the API used the "vm.permissions.clone" element to indicate if the permissions should be
        // cloned, but in V4 this element has been removed and replaced by a "clone_permissions" matrix parameter:
        if (vm.isSetPermissions()) {
            V3Permissions permissions = vm.getPermissions();
            if (permissions.isSetClone() && permissions.isClone()) {
                parameters.put("clone_permissions", String.valueOf(true));
            }
        }

        Response response = adaptAdd(getDelegate()::add, vm);
        V3VmHelper.addDisksLink(response);
        return response;
    }

    @GET
    public V3VMs list(@Context HttpHeaders headers, @Context UriInfo ui) {
        // Transform the data:
        V3VMs vms = adaptList(getDelegate()::list);

        // Add the link to the disks collection:
        vms.getVMs().forEach(V3VmHelper::addDisksLink);

        // Add the requested inline details:
        Set<String> details = DetailHelper.getDetails(headers, ui);
        if (details != null && !details.isEmpty()) {
            vms.getVMs().forEach(vm -> {
                V3VmServer server = getVmResource(vm.getId());
                V3VmHelper.addInlineDetails(vm, server, details);
            });
        }

        return vms;
    }

    @Path("{id}")
    public V3VmServer getVmResource(@PathParam("id") String id) {
        return new V3VmServer(id, getDelegate().getVmResource(id));
    }
}
