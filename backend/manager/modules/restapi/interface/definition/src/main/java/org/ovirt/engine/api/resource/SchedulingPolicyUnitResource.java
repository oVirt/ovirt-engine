package org.ovirt.engine.api.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.SchedulingPolicyUnit;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface SchedulingPolicyUnitResource {
    @GET
    public SchedulingPolicyUnit get();

    @DELETE
    public Response remove();
}
