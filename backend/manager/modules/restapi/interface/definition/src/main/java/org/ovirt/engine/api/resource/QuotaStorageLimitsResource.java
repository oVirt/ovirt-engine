package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.QuotaStorageLimit;
import org.ovirt.engine.api.model.QuotaStorageLimits;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface QuotaStorageLimitsResource {
    @GET
    QuotaStorageLimits list();

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
    Response add(QuotaStorageLimit limit);

    @Path("{id}")
    QuotaStorageLimitResource getLimitResource(@PathParam("id") String id);
}
