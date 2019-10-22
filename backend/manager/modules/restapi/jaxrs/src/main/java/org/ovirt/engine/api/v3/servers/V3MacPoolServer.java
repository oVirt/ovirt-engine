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

import org.ovirt.engine.api.resource.MacPoolResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3MacPool;

@Produces({"application/xml", "application/json"})
public class V3MacPoolServer extends V3Server<MacPoolResource> {
    public V3MacPoolServer(MacPoolResource delegate) {
        super(delegate);
    }

    @GET
    public V3MacPool get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3MacPool update(V3MacPool pool) {
        return adaptUpdate(getDelegate()::update, pool);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }
}
