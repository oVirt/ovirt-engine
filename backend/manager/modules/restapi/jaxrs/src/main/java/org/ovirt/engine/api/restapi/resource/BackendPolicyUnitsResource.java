package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.BaseResources;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;

public abstract class BackendPolicyUnitsResource<M extends BaseResources, N extends BaseResource>
        extends AbstractBackendCollectionResource<N, ClusterPolicy> {

    protected final Guid schedulingPolicyId;

    protected BackendPolicyUnitsResource(Guid schedulingPolicyId,
            Class<N> baseResourcesClass) {
        super(baseResourcesClass, ClusterPolicy.class);
        this.schedulingPolicyId = schedulingPolicyId;
    }

    protected abstract ParametersProvider<N, ClusterPolicy> getAddParametersProvider();

    protected abstract void updateIncomingId(N incoming);

    // need to revisit: update should be in a separate hierarchy
    protected Response performAdd(N incoming) {
        ClusterPolicy entity = getClusterPolicy();
        performAction(ActionType.EditClusterPolicy, getAddParametersProvider().getParameters(incoming, entity));
        entity = getClusterPolicy();
        updateIncomingId(incoming);
        N model = map(entity, incoming);
        model = addLinks(doPopulate(model, entity));
        return Response.ok(model).build();
    }

    protected ClusterPolicy getClusterPolicy() {
        return getEntity(ClusterPolicy.class,
                QueryType.GetClusterPolicyById,
                new IdQueryParameters(schedulingPolicyId),
                schedulingPolicyId.toString());
    }

}
