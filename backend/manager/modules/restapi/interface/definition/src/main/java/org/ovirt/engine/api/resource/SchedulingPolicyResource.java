package org.ovirt.engine.api.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.SchedulingPolicy;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface SchedulingPolicyResource extends UpdatableResource<SchedulingPolicy> {

    @DELETE
    public Response remove();

    @Path("filters")
    public FiltersResource getFiltersResource();

    @Path("weights")
    public WeightsResource getWeightsResource();

    @Path("balances")
    public BalancesResource getBalancesResource();

}
