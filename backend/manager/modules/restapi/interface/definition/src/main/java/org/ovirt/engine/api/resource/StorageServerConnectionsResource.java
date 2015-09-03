package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.model.StorageConnections;

@Path("/storageconnections")
@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
public interface StorageServerConnectionsResource {
    @GET
    public StorageConnections list();

    /**
     * Adds a storage connection entity
     * @param storageConnection -the storage connection to add
     * @return the new newly added storage connection
     */
    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON })
    public Response add(StorageConnection storageConnection);

    /**
     * Sub-resource locator method, returns individual DataCenterResource on which the remainder of the URI is
     * dispatched.
     * @param id  the StorageServerConnection ID
     * @return matching subresource if found
     */
    @Path("{id}")
    public StorageServerConnectionResource getStorageConnectionSubResource(@PathParam("id") String id);
}
