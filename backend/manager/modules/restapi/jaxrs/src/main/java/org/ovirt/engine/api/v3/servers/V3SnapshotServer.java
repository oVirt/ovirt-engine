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
import org.ovirt.engine.api.resource.SnapshotResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3Snapshot;

@Produces({"application/xml", "application/json"})
public class V3SnapshotServer extends V3Server<SnapshotResource> {
    public V3SnapshotServer(SnapshotResource delegate) {
        super(delegate);
    }

    @GET
    public V3Snapshot get() {
        return adaptGet(getDelegate()::get);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("restore")
    public Response restore(V3Action action) {
        return adaptAction(getDelegate()::restore, action);
    }

    @Path("cdroms")
    public V3SnapshotCdromsServer getCdromsResource() {
        return new V3SnapshotCdromsServer(getDelegate().getCdromsResource());
    }

    @Path("disks")
    public V3SnapshotDisksServer getDisksResource() {
        return new V3SnapshotDisksServer(getDelegate().getDisksResource());
    }

    @Path("nics")
    public V3SnapshotNicsServer getNicsResource() {
        return new V3SnapshotNicsServer(getDelegate().getNicsResource());
    }

    @Path("{action: (?:restore)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }

    @Path("creation_status/{oid}")
    public V3CreationServer getCreationResource(@PathParam("oid") String oid) {
        return new V3CreationServer(getDelegate().getCreationResource(oid));
    }
}
