package org.ovirt.engine.api.resource;

import javax.ws.rs.Produces;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface MovableCopyableDiskResource extends DiskResource, MovableResource, CopyableResource {
}
