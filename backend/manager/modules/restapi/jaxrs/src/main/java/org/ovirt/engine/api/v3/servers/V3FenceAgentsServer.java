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

import org.ovirt.engine.api.resource.FenceAgentsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Agent;
import org.ovirt.engine.api.v3.types.V3Agents;

@Produces({"application/xml", "application/json"})
public class V3FenceAgentsServer extends V3Server<FenceAgentsResource> {
    public V3FenceAgentsServer(FenceAgentsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3Agent agent) {
        return adaptAdd(getDelegate()::add, agent);
    }

    @GET
    public V3Agents list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3FenceAgentServer getAgentResource(@PathParam("id") String id) {
        return new V3FenceAgentServer(getDelegate().getAgentResource(id));
    }
}
