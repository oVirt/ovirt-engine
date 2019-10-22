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

import org.ovirt.engine.api.resource.BookmarksResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Bookmark;
import org.ovirt.engine.api.v3.types.V3Bookmarks;

@Produces({"application/xml", "application/json"})
public class V3BookmarksServer extends V3Server<BookmarksResource> {
    public V3BookmarksServer(BookmarksResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3Bookmark bookmark) {
        return adaptAdd(getDelegate()::add, bookmark);
    }

    @GET
    public V3Bookmarks list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3BookmarkServer getBookmarkResource(@PathParam("id") String id) {
        return new V3BookmarkServer(getDelegate().getBookmarkResource(id));
    }
}
