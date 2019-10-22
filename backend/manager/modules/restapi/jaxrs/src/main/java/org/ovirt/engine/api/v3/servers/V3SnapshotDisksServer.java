/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.SnapshotDisksResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Disks;

@Produces({"application/xml", "application/json"})
public class V3SnapshotDisksServer extends V3Server<SnapshotDisksResource> {
    public V3SnapshotDisksServer(SnapshotDisksResource delegate) {
        super(delegate);
    }

    @GET
    public V3Disks list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3SnapshotDiskServer getDiskResource(@PathParam("id") String id) {
        return new V3SnapshotDiskServer(getDelegate().getDiskResource(id));
    }
}
