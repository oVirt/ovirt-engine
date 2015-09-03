package org.ovirt.engine.api.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.IscsiBond;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
public interface IscsiBondResource extends UpdatableResource<IscsiBond> {
    @DELETE
    Response remove();

    @Path("networks")
    NetworksResource getNetworksResource();

    @Path("storageconnections")
    StorageServerConnectionsResource getStorageServerConnectionsResource();
}
