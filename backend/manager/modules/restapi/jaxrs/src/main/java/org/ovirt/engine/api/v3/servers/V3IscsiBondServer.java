/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.IscsiBondResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3IscsiBond;

@Produces({"application/xml", "application/json"})
public class V3IscsiBondServer extends V3Server<IscsiBondResource> {
    public V3IscsiBondServer(IscsiBondResource delegate) {
        super(delegate);
    }

    @GET
    public V3IscsiBond get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3IscsiBond update(V3IscsiBond bond) {
        return adaptUpdate(getDelegate()::update, bond);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @Path("networks")
    public V3NetworksServer getNetworksResource() {
        return new V3NetworksServer(getDelegate().getNetworksResource());
    }

    @Path("storageserverconnections")
    public V3StorageServerConnectionsServer getStorageServerConnectionsResource() {
        return new V3StorageServerConnectionsServer(getDelegate().getStorageServerConnectionsResource());
    }
}
