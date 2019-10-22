/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.aaa.DomainUsersResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Users;

@Produces({"application/xml", "application/json"})
public class V3DomainUsersServer extends V3Server<DomainUsersResource> {
    public V3DomainUsersServer(DomainUsersResource delegate) {
        super(delegate);
    }

    @GET
    public V3Users list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3DomainUserServer getUserResource(@PathParam("id") String id) {
        return new V3DomainUserServer(getDelegate().getUserResource(id));
    }
}
