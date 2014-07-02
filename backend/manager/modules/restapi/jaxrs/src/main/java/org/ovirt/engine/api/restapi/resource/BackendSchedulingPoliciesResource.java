package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.SchedulingPolicies;
import org.ovirt.engine.api.model.SchedulingPolicy;
import org.ovirt.engine.api.resource.SchedulingPoliciesResource;
import org.ovirt.engine.api.resource.SchedulingPolicyResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendSchedulingPoliciesResource extends AbstractBackendCollectionResource<SchedulingPolicy, ClusterPolicy> implements SchedulingPoliciesResource {

    static final String[] SUB_COLLECTIONS = { "filters", "weights", "balances", "clusters" };

    private final QueryIdResolver<Guid> queryIdResolver =
            new QueryIdResolver<Guid>(VdcQueryType.GetClusterPolicyById,
                    IdQueryParameters.class);

    public BackendSchedulingPoliciesResource() {
        super(SchedulingPolicy.class, ClusterPolicy.class, SUB_COLLECTIONS);
    }

    @Override
    public SchedulingPolicies list() {
        List<ClusterPolicy> SchedulingPolicys =
                getBackendCollection(VdcQueryType.GetClusterPolicies, new VdcQueryParametersBase());
        return mapCollection(SchedulingPolicys);
    }

    @Override
    public Response add(SchedulingPolicy schedulingPolicy) {
        validateParameters(schedulingPolicy, "name");
        return performCreate(VdcActionType.AddClusterPolicy, new ClusterPolicyCRUDParameters(null,
                map(schedulingPolicy)), queryIdResolver);
    }

    @Override
    @SingleEntityResource
    public SchedulingPolicyResource getSchedulingPolicySubResource(@PathParam("id") String id) {
        return inject(new BackendSchedulingPolicyResource(id));
    }

    @Override
    protected Response performRemove(String id) {
        Response performAction = null;
        try {

            performAction =
                    performAction(VdcActionType.RemoveClusterPolicy, new ClusterPolicyCRUDParameters(asGuid(id),
                            queryIdResolver.lookupEntity(asGuid(id))));
        } catch (BackendFailureException e) {
            e.printStackTrace();
        }
        return performAction;
    }

    @Override
    protected SchedulingPolicy doPopulate(SchedulingPolicy model, ClusterPolicy entity) {
        return model;
    }

    protected SchedulingPolicies mapCollection(List<ClusterPolicy> entities) {
        SchedulingPolicies collection = new SchedulingPolicies();
        for (ClusterPolicy entity : entities) {
            collection.getSchedulingPolicy().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

}
