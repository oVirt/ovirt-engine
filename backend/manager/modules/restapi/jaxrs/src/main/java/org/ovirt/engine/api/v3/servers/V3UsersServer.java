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

import org.ovirt.engine.api.resource.aaa.UsersResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3User;
import org.ovirt.engine.api.v3.types.V3Users;

@Produces({"application/xml", "application/json"})
public class V3UsersServer extends V3Server<UsersResource> {
    public V3UsersServer(UsersResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3User user) {
        return adaptAdd(getDelegate()::add, user);
    }

    @GET
    public V3Users list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3UserServer getUserResource(@PathParam("id") String id) {
        return new V3UserServer(getDelegate().getUserResource(id));
    }
}
