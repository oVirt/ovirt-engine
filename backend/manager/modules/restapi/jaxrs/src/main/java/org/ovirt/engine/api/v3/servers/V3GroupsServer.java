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

import org.ovirt.engine.api.resource.aaa.GroupsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Group;
import org.ovirt.engine.api.v3.types.V3Groups;

@Produces({"application/xml", "application/json"})
public class V3GroupsServer extends V3Server<GroupsResource> {
    public V3GroupsServer(GroupsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3Group group) {
        return adaptAdd(getDelegate()::add, group);
    }

    @GET
    public V3Groups list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3GroupServer getGroupResource(@PathParam("id") String id) {
        return new V3GroupServer(getDelegate().getGroupResource(id));
    }
}
