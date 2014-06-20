package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.Quotas;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface QuotasResource {

    @GET
    public Quotas list();

    @Path("{id}")
    public QuotaResource getQuotaSubResource(@PathParam("id") String id);

}
