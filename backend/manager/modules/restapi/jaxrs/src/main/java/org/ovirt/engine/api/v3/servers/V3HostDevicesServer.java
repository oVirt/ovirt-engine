/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.HostDevicesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3HostDevices;

@Produces({"application/xml", "application/json"})
public class V3HostDevicesServer extends V3Server<HostDevicesResource> {
    public V3HostDevicesServer(HostDevicesResource delegate) {
        super(delegate);
    }

    @GET
    public V3HostDevices list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3HostDeviceServer getDeviceResource(@PathParam("id") String id) {
        return new V3HostDeviceServer(getDelegate().getDeviceResource(id));
    }
}
