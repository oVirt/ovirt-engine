package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
import org.ovirt.engine.api.model.Agent;
import org.ovirt.engine.api.model.Agents;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.resource.FenceAgentResource;
import org.ovirt.engine.api.resource.FenceAgentsResource;
import org.ovirt.engine.core.common.action.FenceAgentCommandParameterBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendFenceAgentsResource extends AbstractBackendCollectionResource<Agent, FenceAgent> implements FenceAgentsResource {

    protected BackendFenceAgentsResource(String hostId) {
        super(Agent.class, FenceAgent.class);
        this.hostId = hostId;
    }

    private String hostId;

    @Override
    @GET
    @Formatted
    public Agents list() {
        return mapCollection(getFenceAgents());
    }

    @Override
    @POST
    @Formatted
    @Consumes({ "application/xml", "application/json", "application/x-yaml" })
    public Response add(Agent agent) {
        validateParameters(agent, "address", "order", "type", "username", "password");
        return performCreate(VdcActionType.AddFenceAgent,
                getAddParameters(agent),
                new QueryIdResolver<Guid>(VdcQueryType.GetFenceAgentById, IdQueryParameters.class));
    }

    private FenceAgentCommandParameterBase getAddParameters(Agent agent) {
        Host host = new Host();
        host.setId(hostId);
        agent.setHost(host);
        FenceAgentCommandParameterBase params = new FenceAgentCommandParameterBase();
        params.setAgent(map(agent, null));
        return params;
    }

    @Override
    @Path("{id}")
    public FenceAgentResource getFenceAgentSubResource(@PathParam("id") String id) {
        return inject(new BackendFenceAgentResource(id));
    }

    @Override
    protected Response performRemove(String id) {
        FenceAgentCommandParameterBase params = new FenceAgentCommandParameterBase();
        FenceAgent agent = new FenceAgent();
        agent.setId(asGuid(id));
        params.setAgent(agent);
        return performAction(VdcActionType.RemoveFenceAgent, params);
    }

    @Override
    protected Agent doPopulate(Agent model, FenceAgent entity) {
        return model; // no additional information.
    }

    private Agents mapCollection(List<FenceAgent> fenceAgents) {
        Agents agents = new Agents();
        for (FenceAgent fenceAgent : fenceAgents) {
            Agent agent = map(fenceAgent, null);
            agents.getAgents().add(addLinks(populate(agent, fenceAgent)));
        }
        return agents;
    }

    @Override
    public List<FenceAgent> getFenceAgents() {
        return getBackendCollection(VdcQueryType.GetFenceAgentsByVdsId, new IdQueryParameters(new Guid(hostId)));
    }

}
