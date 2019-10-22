/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.SnapshotDiskResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Disk;

@Produces({"application/xml", "application/json"})
public class V3SnapshotDiskServer extends V3Server<SnapshotDiskResource> {
    public V3SnapshotDiskServer(SnapshotDiskResource delegate) {
        super(delegate);
    }

    @GET
    public V3Disk get() {
        return adaptGet(getDelegate()::get);
    }
}
