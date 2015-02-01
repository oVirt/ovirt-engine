package org.ovirt.engine.api.resource.externalhostproviders;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.KatelloErrata;
import org.ovirt.engine.api.resource.ApiMediaType;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface KatelloErrataResource {

    @GET
    KatelloErrata list();

    @Path("{id}")
    KatelloErratumResource getKatelloErratumSubResource(@PathParam("id") String id);
}
