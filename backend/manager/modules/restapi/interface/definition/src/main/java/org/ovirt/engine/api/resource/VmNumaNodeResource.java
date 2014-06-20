package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.VirtualNumaNode;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface VmNumaNodeResource {

    @PUT
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    public VirtualNumaNode update(VirtualNumaNode node);

    @GET
    public VirtualNumaNode get();
}
