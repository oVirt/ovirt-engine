/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.StorageServerConnectionExtensionResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3StorageConnectionExtension;

@Produces({"application/xml", "application/json"})
public class V3StorageServerConnectionExtensionServer extends V3Server<StorageServerConnectionExtensionResource> {
    public V3StorageServerConnectionExtensionServer(StorageServerConnectionExtensionResource delegate) {
        super(delegate);
    }

    @GET
    public V3StorageConnectionExtension get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3StorageConnectionExtension update(V3StorageConnectionExtension extension) {
        return adaptUpdate(getDelegate()::update, extension);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }
}
