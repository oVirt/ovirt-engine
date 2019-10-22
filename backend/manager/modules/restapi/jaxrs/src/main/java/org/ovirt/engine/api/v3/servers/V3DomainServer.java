/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.aaa.DomainResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Domain;

@Produces({"application/xml", "application/json"})
public class V3DomainServer extends V3Server<DomainResource> {
    public V3DomainServer(DomainResource delegate) {
        super(delegate);
    }

    @GET
    public V3Domain get() {
        return adaptGet(getDelegate()::get);
    }

    @Path("users")
    public V3DomainUsersServer getUsersResource() {
        return new V3DomainUsersServer(getDelegate().getUsersResource());
    }

    @Path("groups")
    public V3DomainGroupsServer getGroupsResource() {
        return new V3DomainGroupsServer(getDelegate().getGroupsResource());
    }
}
