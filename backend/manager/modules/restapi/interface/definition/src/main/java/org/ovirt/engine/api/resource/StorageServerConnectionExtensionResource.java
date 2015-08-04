package org.ovirt.engine.api.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.StorageConnectionExtension;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface StorageServerConnectionExtensionResource extends UpdatableResource<StorageConnectionExtension>{

    /**
     * Deletes the connection extension from the system
     *
     * @return
     */
    @DELETE
    public Response remove();
}
