package org.ovirt.engine.api.resource;

import javax.ws.rs.Produces;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
public interface MovableCopyableDiskResource extends DiskResource, MovableResource, CopyableResource {
}
