package org.ovirt.engine.api.resource;

import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.model.StorageConnections;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface StorageDomainServerConnectionsResource {
    @GET
    public StorageConnections list();

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    public Response add(StorageConnection storageConnection);

    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") String id);

    @Path("{id}")
    public StorageDomainServerConnectionResource getStorageConnectionSubResource(@PathParam("id") String id);
}
