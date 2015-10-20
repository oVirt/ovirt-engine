package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.StorageConnection;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
public interface StorageServerConnectionResource {
    @GET
    StorageConnection get();

    @PUT
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
    StorageConnection update(StorageConnection connection);

    /**
     * Deletes the connection from the system, and disconnects the specified host from it.
     */
    @DELETE
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
    Response remove(Action action);

    /**
     * Deletes the connection from the system.
     */
    @DELETE
    Response remove();
}
