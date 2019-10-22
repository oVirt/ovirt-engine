/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.VmReportedDeviceResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3ReportedDevice;

@Produces({"application/xml", "application/json"})
public class V3VmReportedDeviceServer extends V3Server<VmReportedDeviceResource> {
    public V3VmReportedDeviceServer(VmReportedDeviceResource delegate) {
        super(delegate);
    }

    @GET
    public V3ReportedDevice get() {
        return adaptGet(getDelegate()::get);
    }
}
