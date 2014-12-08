package org.ovirt.engine.api.resource;

import org.ovirt.engine.api.model.HostDevices;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface HostDevicesResource {

    @GET
    public HostDevices list();

    @Path("{id}")
    public HostDeviceResource getHostDeviceSubResource(@PathParam("id") String id);
}
