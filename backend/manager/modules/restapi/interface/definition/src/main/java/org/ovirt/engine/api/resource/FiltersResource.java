package org.ovirt.engine.api.resource;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.Filter;
import org.ovirt.engine.api.model.Filters;

@Path("/filters")
@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface FiltersResource extends PolicyUnitsResource<Filters, Filter> {
    @Path("{id}")
    public FilterResource getFilterResource(@PathParam("id") String id);
}
