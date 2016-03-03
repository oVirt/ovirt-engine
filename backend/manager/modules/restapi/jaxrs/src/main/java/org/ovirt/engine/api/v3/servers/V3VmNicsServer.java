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

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;
import static org.ovirt.engine.api.v3.helpers.V3NICHelper.setVnicProfile;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.resource.VmNicsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3NIC;
import org.ovirt.engine.api.v3.types.V3Nics;

@Produces({"application/xml", "application/json"})
public class V3VmNicsServer extends V3Server<VmNicsResource> {
    private String vmId;

    public V3VmNicsServer(String vmId, VmNicsResource delegate) {
        super(delegate);
        this.vmId = vmId;
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3NIC v3Nic) {
        // Convert the NIC to the V4 format:
        Nic v4Nic = adaptIn(v3Nic);

        // Populate the VNIC profile (note that this can't be done in the adapter because in order to determine the
        // candidate VNIC profiles we need to know the identifier of the VM, and that isn't possible in the adapter):
        setVnicProfile(vmId, v3Nic, v4Nic);

        // Pass the modified request to the V4 server (note that we are doing this even if we didn't find a matching
        // VNIC profile, as the V4 server will detect/report/handle the issue better than we can do here):
        try {
            return adaptResponse(getDelegate().add(v4Nic));
        }
        catch (WebApplicationException exception) {
            throw adaptException(exception);
        }
    }

    @GET
    public V3Nics list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3VmNicServer getNicResource(@PathParam("id") String id) {
        return new V3VmNicServer(vmId, getDelegate().getNicResource(id));
    }
}
