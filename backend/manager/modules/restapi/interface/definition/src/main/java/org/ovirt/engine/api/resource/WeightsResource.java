package org.ovirt.engine.api.resource;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.Weight;
import org.ovirt.engine.api.model.Weights;

@Path("/weights")
@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface WeightsResource extends PolicyUnitsResource<Weights, Weight> {
    @Override
    @Path("{id}")
    public WeightResource getSubResource(@PathParam("id") String id);
}
