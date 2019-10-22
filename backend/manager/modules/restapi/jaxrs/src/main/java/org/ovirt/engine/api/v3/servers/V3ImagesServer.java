/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.ImagesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Images;

@Produces({"application/xml", "application/json"})
public class V3ImagesServer extends V3Server<ImagesResource> {
    public V3ImagesServer(ImagesResource delegate) {
        super(delegate);
    }

    @GET
    public V3Images list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3ImageServer getImageResource(@PathParam("id") String id) {
        return new V3ImageServer(getDelegate().getImageResource(id));
    }
}
