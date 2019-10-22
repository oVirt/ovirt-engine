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

import org.ovirt.engine.api.resource.BookmarkResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Bookmark;

@Produces({"application/xml", "application/json"})
public class V3BookmarkServer extends V3Server<BookmarkResource> {
    public V3BookmarkServer(BookmarkResource delegate) {
        super(delegate);
    }

    @GET
    public V3Bookmark get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3Bookmark update(V3Bookmark bookmark) {
        return adaptUpdate(getDelegate()::update, bookmark);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }
}
