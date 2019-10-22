/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.HostStorageResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3HostStorage;

@Produces({"application/xml", "application/json"})
public class V3HostStorageServer extends V3Server<HostStorageResource> {
    public V3HostStorageServer(HostStorageResource delegate) {
        super(delegate);
    }

    @GET
    public V3HostStorage list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3StorageServer getStorageResource(@PathParam("id") String id) {
        return new V3StorageServer(getDelegate().getStorageResource(id));
    }
}
