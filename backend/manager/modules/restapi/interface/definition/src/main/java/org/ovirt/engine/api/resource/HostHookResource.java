package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
import org.ovirt.engine.api.model.Hook;

@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML })
public interface HostHookResource {

    @GET
    @Formatted
    public Hook get();
}
