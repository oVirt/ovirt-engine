package org.ovirt.engine.api.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DiskProfile;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface DiskProfileResource extends UpdatableResource<DiskProfile> {
    @DELETE
    Response remove();

    @Path("permissions")
    public AssignedPermissionsResource getPermissionsResource();
}
