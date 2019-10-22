/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.FenceAgentResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Agent;

@Produces({"application/xml", "application/json"})
public class V3FenceAgentServer extends V3Server<FenceAgentResource> {
    public V3FenceAgentServer(FenceAgentResource delegate) {
        super(delegate);
    }

    @GET
    public V3Agent get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3Agent update(V3Agent agent) {
        return adaptUpdate(getDelegate()::update, agent);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }
}
