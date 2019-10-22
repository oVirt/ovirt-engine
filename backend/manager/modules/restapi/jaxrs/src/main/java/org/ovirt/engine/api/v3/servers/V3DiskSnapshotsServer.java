/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.DiskSnapshotsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3DiskSnapshots;

@Produces({"application/xml", "application/json"})
public class V3DiskSnapshotsServer extends V3Server<DiskSnapshotsResource> {
    public V3DiskSnapshotsServer(DiskSnapshotsResource delegate) {
        super(delegate);
    }

    @GET
    public V3DiskSnapshots list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3DiskSnapshotServer getSnapshotResource(@PathParam("id") String id) {
        return new V3DiskSnapshotServer(getDelegate().getSnapshotResource(id));
    }
}
