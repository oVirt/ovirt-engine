package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.Consumes;

import org.ovirt.engine.api.model.Agent;
import org.ovirt.engine.api.resource.FenceAgentResource;
import org.ovirt.engine.core.common.action.FenceAgentCommandParameterBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendFenceAgentResource extends AbstractBackendSubResource<Agent, FenceAgent> implements FenceAgentResource {

    public BackendFenceAgentResource(String fenceAgentId) {
        super(fenceAgentId, Agent.class, FenceAgent.class);
    }

    @Override
    public Agent get() {
        return performGet(VdcQueryType.GetFenceAgentById, new IdQueryParameters(guid));
    }

    @Override
    @Consumes({ "application/xml", "application/json", "application/x-yaml" })
    public Agent update(Agent agent) {
        QueryIdResolver<Guid> agentResolver =
                new QueryIdResolver<Guid>(VdcQueryType.GetFenceAgentById, IdQueryParameters.class);
        FenceAgent entity = getEntity(agentResolver, true);
        return performUpdate(agent,
                entity,
                map(entity),
                agentResolver,
                VdcActionType.UpdateFenceAgent,
                new UpdateParametersProvider());
    }

    @Override
    protected Agent doPopulate(Agent model, FenceAgent entity) {
        return model; // no additional information required
    }

    protected class UpdateParametersProvider implements ParametersProvider<Agent, FenceAgent> {
        @Override
        public VdcActionParametersBase getParameters(Agent incoming, FenceAgent entity) {
            FenceAgentCommandParameterBase updateParams = new FenceAgentCommandParameterBase();
            FenceAgent agent = map(incoming, entity);
            updateParams.setAgent(agent);
            return updateParams;
        }
    }
}
