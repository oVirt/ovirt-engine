package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.StorageConnection;

public interface StorageServerConnectionResource extends UpdatableResource<StorageConnection> {

    /**
     * Deletes the connection from the system, and disconnects the specified host from it
     *
     * @param action
     * @return
     */
    @DELETE
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    public Response remove(Action action);

    /**
     * Deletes the connection from the system
     *
     * @return
     */
    @DELETE
    public Response remove();

}
