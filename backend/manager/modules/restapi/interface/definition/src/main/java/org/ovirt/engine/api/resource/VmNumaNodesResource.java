package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.VirtualNumaNode;
import org.ovirt.engine.api.model.VirtualNumaNodes;

@Produces({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface VmNumaNodesResource {

    @GET
    public VirtualNumaNodes list();

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    public Response add(VirtualNumaNode node);

    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") String id);

    @Path("{iden}")
    public VmNumaNodeResource getVmNumaNodeSubResource(@PathParam("iden") String id);
}
