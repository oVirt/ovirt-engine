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

import org.ovirt.engine.api.resource.PermitsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Permit;
import org.ovirt.engine.api.v3.types.V3Permits;

@Produces({"application/xml", "application/json"})
public class V3PermitsServer extends V3Server<PermitsResource> {
    public V3PermitsServer(PermitsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3Permit permit) {
        return adaptAdd(getDelegate()::add, permit);
    }

    @GET
    public V3Permits list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3PermitServer getPermitResource(@PathParam("id") String id) {
        return new V3PermitServer(getDelegate().getPermitResource(id));
    }
}
