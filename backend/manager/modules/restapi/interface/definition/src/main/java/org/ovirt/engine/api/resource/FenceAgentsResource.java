package org.ovirt.engine.api.resource;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Agent;
import org.ovirt.engine.api.model.Agents;
import org.ovirt.engine.core.common.businessentities.FenceAgent;

public interface FenceAgentsResource {

    @GET
    public Agents list();

    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") String id);

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    public Response add(Agent agent);

    @Path("{id}")
    public FenceAgentResource getFenceAgentSubResource(@PathParam("id") String id);

    public List<FenceAgent> getFenceAgents();
}
