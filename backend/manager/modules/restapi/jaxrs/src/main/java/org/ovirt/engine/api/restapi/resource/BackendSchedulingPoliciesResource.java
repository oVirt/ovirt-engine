package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.SchedulingPolicies;
import org.ovirt.engine.api.model.SchedulingPolicy;
import org.ovirt.engine.api.resource.SchedulingPoliciesResource;
import org.ovirt.engine.api.resource.SchedulingPolicyResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendSchedulingPoliciesResource extends AbstractBackendCollectionResource<SchedulingPolicy, ClusterPolicy> implements SchedulingPoliciesResource {

    private final QueryIdResolver<Guid> queryIdResolver =
            new QueryIdResolver<>(QueryType.GetClusterPolicyById,
                    IdQueryParameters.class);

    public BackendSchedulingPoliciesResource() {
        super(SchedulingPolicy.class, ClusterPolicy.class);
    }

    @Override
    public SchedulingPolicies list() {
        List<ClusterPolicy> SchedulingPolicys =
                getBackendCollection(QueryType.GetClusterPolicies, new QueryParametersBase());
        return mapCollection(SchedulingPolicys);
    }

    @Override
    public Response add(SchedulingPolicy schedulingPolicy) {
        validateParameters(schedulingPolicy, "name");
        return performCreate(ActionType.AddClusterPolicy, new ClusterPolicyCRUDParameters(null,
                map(schedulingPolicy)), queryIdResolver);
    }

    @Override
    public SchedulingPolicyResource getPolicyResource(String id) {
        return inject(new BackendSchedulingPolicyResource(id));
    }

    protected SchedulingPolicies mapCollection(List<ClusterPolicy> entities) {
        SchedulingPolicies collection = new SchedulingPolicies();
        for (ClusterPolicy entity : entities) {
            collection.getSchedulingPolicies().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

}
