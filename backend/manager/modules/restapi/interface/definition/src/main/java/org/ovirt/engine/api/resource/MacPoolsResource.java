package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.MacPool;
import org.ovirt.engine.api.model.MacPools;

@Path("/macpools")
@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface MacPoolsResource {

    @GET
    public MacPools list();

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
    public Response add(MacPool macPool);

    @Path("{id}")
    public MacPoolResource getMacPoolSubResource(@PathParam("id") String id);
}
