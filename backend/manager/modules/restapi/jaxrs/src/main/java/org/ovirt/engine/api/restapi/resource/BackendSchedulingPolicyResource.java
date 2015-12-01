package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.SchedulingPolicy;
import org.ovirt.engine.api.resource.BalancesResource;
import org.ovirt.engine.api.resource.FiltersResource;
import org.ovirt.engine.api.resource.SchedulingPolicyResource;
import org.ovirt.engine.api.resource.WeightsResource;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendSchedulingPolicyResource extends AbstractBackendSubResource<SchedulingPolicy, ClusterPolicy> implements
        SchedulingPolicyResource {

    private static final String[] SUB_COLLECTIONS = { "filters", "weights", "balances", "clusters" };

    public BackendSchedulingPolicyResource(String id) {
        super(id, SchedulingPolicy.class, ClusterPolicy.class, SUB_COLLECTIONS);
    }

    @Override
    public SchedulingPolicy get() {
        return performGet(VdcQueryType.GetClusterPolicyById, new IdQueryParameters(guid));
    }

    protected ClusterPolicy getSchedulingPolicy() {
        return getEntity(new QueryIdResolver<>(VdcQueryType.GetClusterPolicyById, IdQueryParameters.class), false);
    }

    @Override
    public SchedulingPolicy update(SchedulingPolicy incoming) {
        return performUpdate(incoming,
                new QueryIdResolver<>(VdcQueryType.GetClusterPolicyById, IdQueryParameters.class),
                VdcActionType.EditClusterPolicy,
                new UpdateParametersProvider());
    }

    protected class UpdateParametersProvider implements ParametersProvider<SchedulingPolicy, ClusterPolicy> {
        @Override
        public VdcActionParametersBase getParameters(SchedulingPolicy incoming, ClusterPolicy entity) {
            return new ClusterPolicyCRUDParameters(guid, map(incoming, entity));
        }
    }

    @Override
    public FiltersResource getFiltersResource() {
        return inject(new BackendFiltersResource(guid));
    }

    @Override
    public WeightsResource getWeightsResource() {
        return inject(new BackendWeightsResource(guid));
    }

    @Override
    public BalancesResource getBalancesResource() {
        return inject(new BackendBalancesResource(guid));
    }

    @Override
    public Response remove() {
        get();
        Response performAction = null;
        try {

            performAction =
                    performAction(VdcActionType.RemoveClusterPolicy, new ClusterPolicyCRUDParameters(asGuid(id),
                            new QueryIdResolver<Guid>(VdcQueryType.GetClusterPolicyById,
                                    IdQueryParameters.class).lookupEntity(asGuid(id))));
        } catch (BackendFailureException e) {
            e.printStackTrace();
        }
        return performAction;
    }

}
