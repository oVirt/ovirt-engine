package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Agent;
import org.ovirt.engine.api.model.Agents;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.resource.FenceAgentResource;
import org.ovirt.engine.api.resource.FenceAgentsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.FenceAgentCommandParameterBase;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendFenceAgentsResource
        extends AbstractBackendCollectionResource<Agent, FenceAgent>
        implements FenceAgentsResource {

    private Guid hostId;

    public BackendFenceAgentsResource(Guid hostId) {
        super(Agent.class, FenceAgent.class);
        this.hostId = hostId;
    }

    @Override
    public Agents list() {
        return mapCollection(getFenceAgents());
    }

    @Override
    public Response add(Agent agent) {
        validateParameters(agent, "address", "order", "type", "username", "password");
        return performCreate(ActionType.AddFenceAgent,
                getAddParameters(agent),
                new QueryIdResolver<Guid>(QueryType.GetFenceAgentById, IdQueryParameters.class));
    }

    private FenceAgentCommandParameterBase getAddParameters(Agent agent) {
        Host host = new Host();
        host.setId(hostId.toString());
        agent.setHost(host);
        FenceAgentCommandParameterBase params = new FenceAgentCommandParameterBase();
        params.setAgent(map(agent, null));
        return params;
    }

    @Override
    public FenceAgentResource getAgentResource(String agentId) {
        return inject(new BackendFenceAgentResource(hostId, agentId));
    }

    private Agents mapCollection(List<FenceAgent> fenceAgents) {
        Agents agents = new Agents();
        for (FenceAgent fenceAgent : fenceAgents) {
            Agent agent = map(fenceAgent, null);
            agents.getAgents().add(addLinks(populate(agent, fenceAgent)));
        }
        return agents;
    }

    private List<FenceAgent> getFenceAgents() {
        return getBackendCollection(QueryType.GetFenceAgentsByVdsId, new IdQueryParameters(hostId));
    }
}
