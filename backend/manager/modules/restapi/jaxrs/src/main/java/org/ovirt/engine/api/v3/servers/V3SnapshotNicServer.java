/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.SnapshotNicResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3NIC;

@Produces({"application/xml", "application/json"})
public class V3SnapshotNicServer extends V3Server<SnapshotNicResource> {
    public V3SnapshotNicServer(SnapshotNicResource delegate) {
        super(delegate);
    }

    @GET
    public V3NIC get() {
        return adaptGet(getDelegate()::get);
    }
}
