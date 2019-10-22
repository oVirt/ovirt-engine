/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.VmGraphicsConsoleResource;
import org.ovirt.engine.api.restapi.resource.BackendVmGraphicsConsoleResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3GraphicsConsole;

@Produces({"application/xml", "application/json"})
public class V3VmGraphicsConsoleServer extends V3Server<VmGraphicsConsoleResource> {
    public V3VmGraphicsConsoleServer(VmGraphicsConsoleResource delegate) {
        super(delegate);
    }

    @GET
    public V3GraphicsConsole getXmlOrJson() {
        return adaptGet(getDelegate()::get);
    }

    @GET
    @Produces("application/x-virt-viewer")
    public Response getXVirtViewer() {
        BackendVmGraphicsConsoleResource delegate = (BackendVmGraphicsConsoleResource) getDelegate();
        return delegate.generateDescriptor();
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("proxyticket")
    public Response proxyTicket(V3Action action) {
        return adaptAction(getDelegate()::proxyTicket, action);
    }

    @Path("{action: (?:proxyticket)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
