package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.NetworkAttachment;
import org.ovirt.engine.api.model.NetworkAttachments;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
public interface NetworkAttachmentsResource {

    @GET
    NetworkAttachments list();

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
    Response add(NetworkAttachment networkAttachment);

    @Path("{id}")
    NetworkAttachmentResource getNetworkAttachmentSubResource(@PathParam("id") String id);
}
