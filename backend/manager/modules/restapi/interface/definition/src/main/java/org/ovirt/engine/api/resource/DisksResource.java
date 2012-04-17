package org.ovirt.engine.api.resource;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;

public interface DisksResource extends DevicesResource<Disk, Disks>{

    @Path("{iden}")
    @Override
    public DiskResource getDeviceSubResource(@PathParam("iden") String id);
}
