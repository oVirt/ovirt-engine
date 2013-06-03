package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;

public interface VmDisksResource extends DevicesResource<Disk, Disks>{

    @Path("{iden}")
    @Override
    public VmDiskResource getDeviceSubResource(@PathParam("iden") String id);

    @DELETE
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Path("{iden}")
    public Response remove(@PathParam("iden") String id, Action action);
}
