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
import org.ovirt.engine.api.resource.AttachedStorageDomainResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3StorageDomain;

@Produces({"application/xml", "application/json"})
public class V3AttachedStorageDomainServer extends V3Server<AttachedStorageDomainResource> {
    public V3AttachedStorageDomainServer(AttachedStorageDomainResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("activate")
    public Response activate(V3Action action) {
        return adaptAction(getDelegate()::activate, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("deactivate")
    public Response deactivate(V3Action action) {
        return adaptAction(getDelegate()::deactivate, action);
    }

    @GET
    public V3StorageDomain get() {
        return adaptGet(getDelegate()::get);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @Path("disks")
    public V3AttachedStorageDomainDisksServer getDisksResource() {
        return new V3AttachedStorageDomainDisksServer(getDelegate().getDisksResource());
    }

    @Path("{action: (?:activate|deactivate)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
