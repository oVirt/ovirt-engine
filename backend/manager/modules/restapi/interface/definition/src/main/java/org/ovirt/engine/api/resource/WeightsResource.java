package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.Weight;
import org.ovirt.engine.api.model.Weights;

@Path("/weights")
@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface WeightsResource {
    @GET
    Weights list();

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
    Weight add(Weight weight);

    @Path("{id}")
    WeightResource getWeightResource(@PathParam("id") String id);
}
