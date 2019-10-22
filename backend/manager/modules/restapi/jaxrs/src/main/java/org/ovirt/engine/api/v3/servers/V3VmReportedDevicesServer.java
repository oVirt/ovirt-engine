/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.VmReportedDevicesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3ReportedDevices;

@Produces({"application/xml", "application/json"})
public class V3VmReportedDevicesServer extends V3Server<VmReportedDevicesResource> {
    public V3VmReportedDevicesServer(VmReportedDevicesResource delegate) {
        super(delegate);
    }

    @GET
    public V3ReportedDevices list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3VmReportedDeviceServer getReportedDeviceResource(@PathParam("id") String id) {
        return new V3VmReportedDeviceServer(getDelegate().getReportedDeviceResource(id));
    }

}
