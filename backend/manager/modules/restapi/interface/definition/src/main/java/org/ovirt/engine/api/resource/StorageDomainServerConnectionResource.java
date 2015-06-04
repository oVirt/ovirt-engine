package org.ovirt.engine.api.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.StorageConnection;

public interface StorageDomainServerConnectionResource {
    @GET
    public StorageConnection get();

    @DELETE
    public Response remove();
}
