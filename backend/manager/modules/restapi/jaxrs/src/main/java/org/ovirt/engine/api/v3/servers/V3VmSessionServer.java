/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.VmSessionResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Session;

@Produces({"application/xml", "application/json"})
public class V3VmSessionServer extends V3Server<VmSessionResource> {
    public V3VmSessionServer(VmSessionResource delegate) {
        super(delegate);
    }

    @GET
    public V3Session get() {
        return adaptGet(getDelegate()::get);
    }
}
