package org.ovirt.engine.api.resource;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
public interface VmDisksResource extends DevicesResource<Disk, Disks>{
    @Path("{iden}")
    @Override
    VmDiskResource getDeviceResource(@PathParam("iden") String id);
}
