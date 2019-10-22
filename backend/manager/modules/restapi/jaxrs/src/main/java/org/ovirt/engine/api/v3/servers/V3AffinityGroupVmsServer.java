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

import org.ovirt.engine.api.resource.AffinityGroupVmsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3VM;
import org.ovirt.engine.api.v3.types.V3VMs;

@Produces({"application/xml", "application/json"})
public class V3AffinityGroupVmsServer extends V3Server<AffinityGroupVmsResource> {
    public V3AffinityGroupVmsServer(AffinityGroupVmsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3VM vm) {
        return adaptAdd(getDelegate()::add, vm);
    }

    @GET
    public V3VMs list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3AffinityGroupVmServer getVmResource(@PathParam("id") String id) {
        return new V3AffinityGroupVmServer(getDelegate().getVmResource(id));
    }
}
