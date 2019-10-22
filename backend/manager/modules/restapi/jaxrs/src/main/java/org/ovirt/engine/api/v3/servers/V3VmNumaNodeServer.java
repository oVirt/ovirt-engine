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

import org.ovirt.engine.api.resource.VmNumaNodeResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3VirtualNumaNode;

@Produces({"application/xml", "application/json"})
public class V3VmNumaNodeServer extends V3Server<VmNumaNodeResource> {
    public V3VmNumaNodeServer(VmNumaNodeResource delegate) {
        super(delegate);
    }

    @GET
    public V3VirtualNumaNode get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3VirtualNumaNode update(V3VirtualNumaNode node) {
        return adaptUpdate(getDelegate()::update, node);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }
}
