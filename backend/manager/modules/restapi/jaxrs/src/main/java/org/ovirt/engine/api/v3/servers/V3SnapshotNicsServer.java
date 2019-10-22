/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.SnapshotNicsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Nics;

@Produces({"application/xml", "application/json"})
public class V3SnapshotNicsServer extends V3Server<SnapshotNicsResource> {
    public V3SnapshotNicsServer(SnapshotNicsResource delegate) {
        super(delegate);
    }

    @GET
    public V3Nics list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3SnapshotNicServer getNicResource(@PathParam("id") String id) {
        return new V3SnapshotNicServer(getDelegate().getNicResource(id));
    }
}
