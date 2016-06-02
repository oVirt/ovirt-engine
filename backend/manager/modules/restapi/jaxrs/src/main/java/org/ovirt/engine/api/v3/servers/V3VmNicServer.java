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
import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;
import static org.ovirt.engine.api.v3.helpers.V3NICHelper.setVnicProfile;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.resource.VmNicResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3NIC;

@Produces({"application/xml", "application/json"})
public class V3VmNicServer extends V3Server<VmNicResource> {
    private String vmId;

    public V3VmNicServer(String vmId, VmNicResource delegate) {
        super(delegate);
        this.vmId = vmId;
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("activate")
    public Response activate(V3Action action) {
        return adaptAction(getDelegate()::activate, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("deactivate")
    public Response deactivate(V3Action action) {
        return adaptAction(getDelegate()::deactivate, action);
    }

    @GET
    public V3NIC get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3NIC update(V3NIC v3Nic) {
        // Convert the NIC to the V4 format:
        Nic v4Nic = adaptIn(v3Nic);

        // In version 4 of the API changes to the network or port mirroring configuration of the NIC require a change
        // of the VNIC profile, so if any of those attributes is present in the update sent by the user we need to find
        // a VNIC profile that is compatible, and assign it.
        if (v3Nic.isSetNetwork() || v3Nic.isSetPortMirroring()) {
            setVnicProfile(vmId, v3Nic, v4Nic);
        }

        // Pass the modified request to the V4 server:
        try {
            return adaptOut(getDelegate().update(v4Nic));
        }
        catch (WebApplicationException exception) {
            throw adaptException(exception);
        }
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @Path("reporteddevices")
    public V3VmReportedDevicesServer getReportedDevicesResource() {
        return new V3VmReportedDevicesServer(getDelegate().getReportedDevicesResource());
    }

    @Path("statistics")
    public V3StatisticsServer getStatisticsResource() {
        return new V3StatisticsServer(getDelegate().getStatisticsResource());
    }

    @Path("{action: (activate|deactivate)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
