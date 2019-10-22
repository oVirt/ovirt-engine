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

import org.ovirt.engine.api.resource.NetworkAttachmentResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3NetworkAttachment;

@Produces({"application/xml", "application/json"})
public class V3NetworkAttachmentServer extends V3Server<NetworkAttachmentResource> {
    public V3NetworkAttachmentServer(NetworkAttachmentResource delegate) {
        super(delegate);
    }

    @GET
    public V3NetworkAttachment get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3NetworkAttachment update(V3NetworkAttachment attachment) {
        return adaptUpdate(getDelegate()::update, attachment);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }
}
