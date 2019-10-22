/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.IconsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Icons;

@Produces({"application/xml", "application/json"})
public class V3IconsServer extends V3Server<IconsResource> {
    public V3IconsServer(IconsResource delegate) {
        super(delegate);
    }

    @GET
    public V3Icons list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3IconServer getIconResource(@PathParam("id") String id) {
        return new V3IconServer(getDelegate().getIconResource(id));
    }
}
