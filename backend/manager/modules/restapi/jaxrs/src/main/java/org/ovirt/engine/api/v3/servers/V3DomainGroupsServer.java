/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.aaa.DomainGroupsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Groups;

@Produces({"application/xml", "application/json"})
public class V3DomainGroupsServer extends V3Server<DomainGroupsResource> {
    public V3DomainGroupsServer(DomainGroupsResource delegate) {
        super(delegate);
    }

    @GET
    public V3Groups list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3DomainGroupServer getGroupResource(@PathParam("id") String id) {
        return new V3DomainGroupServer(getDelegate().getGroupResource(id));
    }
}
