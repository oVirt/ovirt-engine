package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.StorageConnectionExtension;
import org.ovirt.engine.api.model.StorageConnectionExtensions;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface StorageServerConnectionExtensionsResource {
    @GET
    public StorageConnectionExtensions list();

    /**
     * Adds a storage connection extension entity
     * @param storageConnectionExtension -the storage connection extension to add
     * @return the new newly added storage connection extension
     */
    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    public Response add(StorageConnectionExtension storageConnectionExtension);

    /**
     * Sub-resource locator method, returns individual DataCenterResource on which the remainder of the URI is
     * dispatched.
     * @param id  The storage connection extension ID.
     * @return matching subresource if found
     */
    @Path("{id}")
    public StorageServerConnectionExtensionResource getStorageConnectionExtensionSubResource(@PathParam("id") String id);
}
