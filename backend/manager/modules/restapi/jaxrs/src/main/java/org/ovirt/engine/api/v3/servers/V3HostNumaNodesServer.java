/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.HostNumaNodesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3NumaNodes;

@Produces({"application/xml", "application/json"})
public class V3HostNumaNodesServer extends V3Server<HostNumaNodesResource> {
    public V3HostNumaNodesServer(HostNumaNodesResource delegate) {
        super(delegate);
    }

    @GET
    public V3NumaNodes list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3HostNumaNodeServer getNodeResource(@PathParam("id") String id) {
        return new V3HostNumaNodeServer(getDelegate().getNodeResource(id));
    }
}
