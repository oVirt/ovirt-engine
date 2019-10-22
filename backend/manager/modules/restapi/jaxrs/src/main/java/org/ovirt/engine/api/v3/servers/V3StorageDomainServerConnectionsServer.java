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

import org.ovirt.engine.api.resource.StorageDomainServerConnectionsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3StorageConnection;
import org.ovirt.engine.api.v3.types.V3StorageConnections;

@Produces({"application/xml", "application/json"})
public class V3StorageDomainServerConnectionsServer extends V3Server<StorageDomainServerConnectionsResource> {
    public V3StorageDomainServerConnectionsServer(StorageDomainServerConnectionsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3StorageConnection connection) {
        return adaptAdd(getDelegate()::add, connection);
    }

    @GET
    public V3StorageConnections list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3StorageDomainServerConnectionServer getConnectionResource(@PathParam("id") String id) {
        return new V3StorageDomainServerConnectionServer(getDelegate().getConnectionResource(id));
    }
}
