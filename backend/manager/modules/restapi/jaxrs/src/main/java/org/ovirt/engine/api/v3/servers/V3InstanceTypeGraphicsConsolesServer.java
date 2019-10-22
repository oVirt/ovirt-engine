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

import org.ovirt.engine.api.resource.InstanceTypeGraphicsConsolesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3GraphicsConsole;
import org.ovirt.engine.api.v3.types.V3GraphicsConsoles;

@Produces({"application/xml", "application/json"})
public class V3InstanceTypeGraphicsConsolesServer extends V3Server<InstanceTypeGraphicsConsolesResource> {
    public V3InstanceTypeGraphicsConsolesServer(InstanceTypeGraphicsConsolesResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3GraphicsConsole console) {
        return adaptAdd(getDelegate()::add, console);
    }

    @GET
    public V3GraphicsConsoles list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3InstanceTypeGraphicsConsoleServer getConsoleResource(@PathParam("id") String id) {
        return new V3InstanceTypeGraphicsConsoleServer(getDelegate().getConsoleResource(id));
    }
}
