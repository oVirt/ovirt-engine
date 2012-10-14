package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;

@Path("/disks")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
public interface DisksResource extends ReadOnlyDevicesResource<Disk, Disks> {

    @Path("{identity}")
    @Override
    public DiskResource getDeviceSubResource(@PathParam("identity") String id);

    @POST
    @Formatted
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML })
    public Response add(Disk device);

    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") String id);

}
