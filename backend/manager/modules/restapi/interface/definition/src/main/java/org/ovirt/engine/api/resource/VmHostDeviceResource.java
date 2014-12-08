package org.ovirt.engine.api.resource;

import org.ovirt.engine.api.model.HostDevice;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface VmHostDeviceResource {

    @GET
    public HostDevice get();

    @DELETE
    public Response remove();
}
