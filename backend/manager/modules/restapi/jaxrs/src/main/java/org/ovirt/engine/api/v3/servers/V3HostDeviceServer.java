/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.HostDeviceResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3HostDevice;

@Produces({"application/xml", "application/json"})
public class V3HostDeviceServer extends V3Server<HostDeviceResource> {
    public V3HostDeviceServer(HostDeviceResource delegate) {
        super(delegate);
    }

    @GET
    public V3HostDevice get() {
        return adaptGet(getDelegate()::get);
    }
}
