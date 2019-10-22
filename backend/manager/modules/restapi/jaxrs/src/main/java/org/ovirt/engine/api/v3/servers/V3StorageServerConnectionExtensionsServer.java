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

import org.ovirt.engine.api.resource.StorageServerConnectionExtensionsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3StorageConnectionExtension;
import org.ovirt.engine.api.v3.types.V3StorageConnectionExtensions;

@Produces({"application/xml", "application/json"})
public class V3StorageServerConnectionExtensionsServer extends V3Server<StorageServerConnectionExtensionsResource> {
    public V3StorageServerConnectionExtensionsServer(StorageServerConnectionExtensionsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3StorageConnectionExtension extension) {
        return adaptAdd(getDelegate()::add, extension);
    }

    @GET
    public V3StorageConnectionExtensions list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3StorageServerConnectionExtensionServer getStorageConnectionExtensionResource(@PathParam("id") String id) {
        return new V3StorageServerConnectionExtensionServer(getDelegate().getStorageConnectionExtensionResource(id));
    }
}
