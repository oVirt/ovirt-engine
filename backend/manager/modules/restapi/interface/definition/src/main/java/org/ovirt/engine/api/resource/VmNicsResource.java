package org.ovirt.engine.api.resource;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;

@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML })
public interface VmNicsResource extends DevicesResource<NIC, Nics> {

    @Path("{iden}")
    @Override
    public VmNicResource getDeviceSubResource(@PathParam("iden") String id);
}
