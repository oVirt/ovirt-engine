/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.RoleResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Role;

@Produces({"application/xml", "application/json"})
public class V3RoleServer extends V3Server<RoleResource> {
    public V3RoleServer(RoleResource delegate) {
        super(delegate);
    }

    @GET
    public V3Role get() {
        return adaptGet(getDelegate()::get);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3Role update(V3Role role) {
        return adaptUpdate(getDelegate()::update, role);
    }

    @Path("permits")
    public V3PermitsServer getPermitsResource() {
        return new V3PermitsServer(getDelegate().getPermitsResource());
    }
}
