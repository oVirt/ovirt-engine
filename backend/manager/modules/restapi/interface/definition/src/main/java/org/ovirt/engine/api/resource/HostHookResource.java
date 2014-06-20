package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.Hook;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface HostHookResource {

    @GET
    public Hook get();
}
