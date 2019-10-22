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

import org.ovirt.engine.api.resource.NetworkAttachmentsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3NetworkAttachment;
import org.ovirt.engine.api.v3.types.V3NetworkAttachments;

@Produces({"application/xml", "application/json"})
public class V3NetworkAttachmentsServer extends V3Server<NetworkAttachmentsResource> {
    public V3NetworkAttachmentsServer(NetworkAttachmentsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3NetworkAttachment attachment) {
        return adaptAdd(getDelegate()::add, attachment);
    }

    @GET
    public V3NetworkAttachments list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3NetworkAttachmentServer getAttachmentResource(@PathParam("id") String id) {
        return new V3NetworkAttachmentServer(getDelegate().getAttachmentResource(id));
    }
}
