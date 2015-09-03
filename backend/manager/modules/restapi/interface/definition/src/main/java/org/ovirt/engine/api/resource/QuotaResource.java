package org.ovirt.engine.api.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Quota;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
public interface QuotaResource extends UpdatableResource<Quota> {
    @DELETE
    public Response remove();

    @Path("quotastoragelimits")
    public QuotaStorageLimitsResource getQuotaStorageLimitsResource();

    @Path("quotaclusterlimits")
    public QuotaClusterLimitsResource getQuotaClusterLimitsResource();

    @Path("permissions")
    public AssignedPermissionsResource getPermissionsResource();
}
