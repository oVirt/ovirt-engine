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

import org.ovirt.engine.api.resource.TagsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Tag;
import org.ovirt.engine.api.v3.types.V3Tags;

@Produces({"application/xml", "application/json"})
public class V3TagsServer extends V3Server<TagsResource> {
    public V3TagsServer(TagsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3Tag tag) {
        return adaptAdd(getDelegate()::add, tag);
    }

    @GET
    public V3Tags list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3TagServer getTagResource(@PathParam("id") String id) {
        return new V3TagServer(getDelegate().getTagResource(id));
    }
}
