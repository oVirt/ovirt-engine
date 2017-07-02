package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.SchedulingPolicy;
import org.ovirt.engine.api.resource.BalancesResource;
import org.ovirt.engine.api.resource.FiltersResource;
import org.ovirt.engine.api.resource.SchedulingPolicyResource;
import org.ovirt.engine.api.resource.WeightsResource;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendSchedulingPolicyResource extends AbstractBackendSubResource<SchedulingPolicy, ClusterPolicy> implements
        SchedulingPolicyResource {

    private static final Logger log = LoggerFactory.getLogger(BackendSchedulingPolicyResource.class);

    public BackendSchedulingPolicyResource(String id) {
        super(id, SchedulingPolicy.class, ClusterPolicy.class);
    }

    @Override
    public SchedulingPolicy get() {
        return performGet(QueryType.GetClusterPolicyById, new IdQueryParameters(guid));
    }

    protected ClusterPolicy getSchedulingPolicy() {
        return getEntity(new QueryIdResolver<>(QueryType.GetClusterPolicyById, IdQueryParameters.class), false);
    }

    @Override
    public SchedulingPolicy update(SchedulingPolicy incoming) {
        return performUpdate(incoming,
                new QueryIdResolver<>(QueryType.GetClusterPolicyById, IdQueryParameters.class),
                ActionType.EditClusterPolicy,
                new UpdateParametersProvider());
    }

    protected class UpdateParametersProvider implements ParametersProvider<SchedulingPolicy, ClusterPolicy> {
        @Override
        public ActionParametersBase getParameters(SchedulingPolicy incoming, ClusterPolicy entity) {
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
                    performAction(ActionType.RemoveClusterPolicy, new ClusterPolicyCRUDParameters(asGuid(id),
                            new QueryIdResolver<Guid>(QueryType.GetClusterPolicyById,
                                    IdQueryParameters.class).lookupEntity(asGuid(id))));
        } catch (BackendFailureException e) {
            log.error("Failed performing action", e);
        }
        return performAction;
    }

}
