/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.gluster.GlusterHooksResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3GlusterHooks;

@Produces({"application/xml", "application/json"})
public class V3GlusterHooksServer extends V3Server<GlusterHooksResource> {
    public V3GlusterHooksServer(GlusterHooksResource delegate) {
        super(delegate);
    }

    @GET
    public V3GlusterHooks list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3GlusterHookServer getHookResource(@PathParam("id") String id) {
        return new V3GlusterHookServer(getDelegate().getHookResource(id));
    }
}
