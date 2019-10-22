/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.HostHooksResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Hooks;

@Produces({"application/xml", "application/json"})
public class V3HostHooksServer extends V3Server<HostHooksResource> {
    public V3HostHooksServer(HostHooksResource delegate) {
        super(delegate);
    }

    @GET
    public V3Hooks list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3HostHookServer getHookResource(@PathParam("id") String id) {
        return new V3HostHookServer(getDelegate().getHookResource(id));
    }
}
