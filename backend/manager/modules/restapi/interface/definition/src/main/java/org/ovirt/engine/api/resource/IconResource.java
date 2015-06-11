package org.ovirt.engine.api.resource;

import org.ovirt.engine.api.model.Icon;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface IconResource {

    @GET
    Icon get();
}
