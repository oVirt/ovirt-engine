package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Quota;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
public interface QuotaResource {
    @GET
    Quota get();

    @PUT
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
    Quota update(Quota quota);

    @DELETE
    Response remove();

    @Path("quotastoragelimits")
    QuotaStorageLimitsResource getQuotaStorageLimitsResource();

    @Path("quotaclusterlimits")
    QuotaClusterLimitsResource getQuotaClusterLimitsResource();

    @Path("permissions")
    AssignedPermissionsResource getPermissionsResource();
}
