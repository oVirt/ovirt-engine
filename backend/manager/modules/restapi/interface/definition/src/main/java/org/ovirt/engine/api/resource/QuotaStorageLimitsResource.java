package org.ovirt.engine.api.resource;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.QuotaStorageLimit;
import org.ovirt.engine.api.model.QuotaStorageLimits;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface QuotaStorageLimitsResource extends QuotaLimitsResource<QuotaStorageLimits, QuotaStorageLimit> {
    @Path("{id}")
    public QuotaStorageLimitResource getLimitResource(@PathParam("id") String id);
}
