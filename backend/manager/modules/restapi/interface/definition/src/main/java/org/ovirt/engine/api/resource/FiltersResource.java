package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.Filter;
import org.ovirt.engine.api.model.Filters;

@Path("/filters")
@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface FiltersResource {
    @GET
    Filters list();

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
    Filter add(Filter filter);

    @Path("{id}")
    FilterResource getFilterResource(@PathParam("id") String id);
}
