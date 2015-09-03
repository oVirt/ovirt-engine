package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.Disk;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
public interface StorageDomainContentDiskResource extends ReadOnlyDeviceResource<Disk> {
    @GET
    @Override
    public Disk get();
}
