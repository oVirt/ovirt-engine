package org.ovirt.engine.api.resource;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.IscsiBond;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface IscsiBondResource extends UpdatableResource<IscsiBond> {

    @Path("networks")
    public NetworksResource getNetworksResource();

    @Path("storageconnections")
    public StorageServerConnectionsResource getStorageServerConnectionsResource();
}
