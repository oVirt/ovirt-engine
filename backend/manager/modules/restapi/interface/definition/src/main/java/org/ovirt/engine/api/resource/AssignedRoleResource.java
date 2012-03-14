package org.ovirt.engine.api.resource;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;


@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
public interface AssignedRoleResource {

    @Path("permits")
    public PermitsResource getPermitsResource();

}
