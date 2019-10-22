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

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.ImageResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3Image;

@Produces({"application/xml", "application/json"})
public class V3ImageServer extends V3Server<ImageResource> {
    public V3ImageServer(ImageResource delegate) {
        super(delegate);
    }

    @GET
    public V3Image get() {
        return adaptGet(getDelegate()::get);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("import")
    public Response doImport(V3Action action) {
        return adaptAction(getDelegate()::doImport, action);
    }

    @Path("{action: (?:import)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
