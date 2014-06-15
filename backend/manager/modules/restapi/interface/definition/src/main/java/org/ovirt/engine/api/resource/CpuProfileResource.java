package org.ovirt.engine.api.resource;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.CpuProfile;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface CpuProfileResource extends UpdatableResource<CpuProfile> {

    @Path("permissions")
    public AssignedPermissionsResource getPermissionsResource();
}
