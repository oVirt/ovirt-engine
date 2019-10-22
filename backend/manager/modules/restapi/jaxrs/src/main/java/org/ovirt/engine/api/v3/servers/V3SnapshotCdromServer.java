/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.SnapshotCdromResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3CdRom;

@Produces({"application/xml", "application/json"})
public class V3SnapshotCdromServer extends V3Server<SnapshotCdromResource> {
    public V3SnapshotCdromServer(SnapshotCdromResource delegate) {
        super(delegate);
    }

    @GET
    public V3CdRom get() {
        return adaptGet(getDelegate()::get);
    }
}
