/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.HostHookResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Hook;

@Produces({"application/xml", "application/json"})
public class V3HostHookServer extends V3Server<HostHookResource> {
    public V3HostHookServer(HostHookResource delegate) {
        super(delegate);
    }

    @GET
    public V3Hook get() {
        return adaptGet(getDelegate()::get);
    }
}
