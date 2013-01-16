package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
import org.ovirt.engine.api.model.Disk;

@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
public interface StorageDomainContentDiskResource extends ReadOnlyDeviceResource<Disk> {
    @GET
    @Formatted
    @Override
    public Disk get();
}
