package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.HostDevices;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
public interface HostDevicesResource {

    @GET
    public HostDevices list();

    @Path("{id}")
    public HostDeviceResource getHostDeviceResource(@PathParam("id") String id);
}
