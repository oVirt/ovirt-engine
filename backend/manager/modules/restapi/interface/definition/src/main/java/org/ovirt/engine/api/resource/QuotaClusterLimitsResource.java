package org.ovirt.engine.api.resource;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.QuotaClusterLimit;
import org.ovirt.engine.api.model.QuotaClusterLimits;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface QuotaClusterLimitsResource extends QuotaLimitsResource<QuotaClusterLimits, QuotaClusterLimit> {
    @Path("{id}")
    public QuotaClusterLimitResource getLimitResource(@PathParam("id") String id);
}
