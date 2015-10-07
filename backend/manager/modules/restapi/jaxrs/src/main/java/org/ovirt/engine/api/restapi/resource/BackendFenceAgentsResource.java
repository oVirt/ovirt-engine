package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

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

public class BackendFenceAgentsResource
        extends AbstractBackendCollectionResource<Agent, FenceAgent>
        implements FenceAgentsResource {

    protected BackendFenceAgentsResource(String hostId) {
        super(Agent.class, FenceAgent.class);
        this.hostId = hostId;
    }

    private String hostId;

    @Override
    public Agents list() {
        return mapCollection(getFenceAgents());
    }

    @Override
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
    public FenceAgentResource getAgentResource(String id) {
        return inject(new BackendFenceAgentResource(id));
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
        return getBackendCollection(VdcQueryType.GetFenceAgentsByVdsId, new IdQueryParameters(new Guid(hostId)));
    }
}
