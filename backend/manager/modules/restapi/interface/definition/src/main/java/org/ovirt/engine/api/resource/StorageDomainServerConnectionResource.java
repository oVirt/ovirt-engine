package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;

import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
import org.ovirt.engine.api.model.StorageConnection;

public interface StorageDomainServerConnectionResource {
    @GET
    @Formatted
    public StorageConnection get();

}
