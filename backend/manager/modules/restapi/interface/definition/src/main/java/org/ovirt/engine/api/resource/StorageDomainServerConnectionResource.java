package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;

import org.ovirt.engine.api.model.StorageConnection;

public interface StorageDomainServerConnectionResource {
    @GET
    public StorageConnection get();

}
