package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;

@Path("/disks")
@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface DisksResource extends ReadOnlyDevicesResource<Disk, Disks> {

    @Path("{identity}")
    @Override
    public MovableCopyableDiskResource getDeviceSubResource(@PathParam("identity") String id);

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    public Response add(Disk device);

    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") String id);

}
