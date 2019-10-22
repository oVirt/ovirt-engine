/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.VmHostDevicesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3HostDevice;
import org.ovirt.engine.api.v3.types.V3HostDevices;

@Produces({"application/xml", "application/json"})
public class V3VmHostDevicesServer extends V3Server<VmHostDevicesResource> {
    public V3VmHostDevicesServer(VmHostDevicesResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3HostDevice device) {
        return adaptAdd(getDelegate()::add, device);
    }

    @GET
    public V3HostDevices list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3VmHostDeviceServer getDeviceResource(@PathParam("id") String id) {
        return new V3VmHostDeviceServer(getDelegate().getDeviceResource(id));
    }
}
