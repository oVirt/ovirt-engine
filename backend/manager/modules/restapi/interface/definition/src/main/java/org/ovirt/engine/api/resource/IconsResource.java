package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.Icons;

@Path("/icons")
@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
public interface IconsResource {

    @GET Icons list();

    @Path("{id}")
    IconResource getVmIconSubResource(@PathParam("id") String id);
}
