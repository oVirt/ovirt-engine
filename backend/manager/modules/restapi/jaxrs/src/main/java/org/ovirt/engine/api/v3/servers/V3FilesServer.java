/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.FilesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Files;

@Produces({"application/xml", "application/json"})
public class V3FilesServer extends V3Server<FilesResource> {
    public V3FilesServer(FilesResource delegate) {
        super(delegate);
    }

    @GET
    public V3Files list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3FileServer getFileResource(@PathParam("id") String id) {
        return new V3FileServer(getDelegate().getFileResource(id));
    }
}
