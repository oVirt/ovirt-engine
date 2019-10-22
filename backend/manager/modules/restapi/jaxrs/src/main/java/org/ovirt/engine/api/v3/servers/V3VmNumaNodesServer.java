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

import org.ovirt.engine.api.resource.VmNumaNodesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3VirtualNumaNode;
import org.ovirt.engine.api.v3.types.V3VirtualNumaNodes;

@Produces({"application/xml", "application/json"})
public class V3VmNumaNodesServer extends V3Server<VmNumaNodesResource> {
    public V3VmNumaNodesServer(VmNumaNodesResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3VirtualNumaNode node) {
        return adaptAdd(getDelegate()::add, node);
    }

    @GET
    public V3VirtualNumaNodes list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3VmNumaNodeServer getNodeResource(@PathParam("id") String id) {
        return new V3VmNumaNodeServer(getDelegate().getNodeResource(id));
    }
}
