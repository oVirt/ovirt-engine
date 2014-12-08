package org.ovirt.engine.api.resource;

import org.ovirt.engine.api.model.HostDevice;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface HostDeviceResource {

    @GET
    public HostDevice get();
}
