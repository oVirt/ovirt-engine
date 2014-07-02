package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.BaseResources;
import org.ovirt.engine.api.resource.PolicyUnitsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public abstract class BackendPolicyUnitsResource<M extends BaseResources, N extends BaseResource> extends AbstractBackendCollectionResource<N, ClusterPolicy> implements PolicyUnitsResource<M, N> {

    protected final Guid schedulingPolicyId;
    private static final String[] SUB_COLLECTIONS = {};

    protected BackendPolicyUnitsResource(Guid schedulingPolicyId,
            Class<N> baseResourcesClass) {
        super(baseResourcesClass, ClusterPolicy.class, SUB_COLLECTIONS);
        this.schedulingPolicyId = schedulingPolicyId;
    }

    protected abstract ParametersProvider<N, ClusterPolicy> getAddParametersProvider();

    protected abstract void updateEntityForRemove(ClusterPolicy entity, Guid id);

    protected abstract void updateIncomingId(N incoming);

    @Override
    protected Response performRemove(String id) {
        ClusterPolicy entity = getClusterPolicy();
        updateEntityForRemove(entity, asGuid(id));
        return performAction(VdcActionType.EditClusterPolicy,
                new ClusterPolicyCRUDParameters(entity.getId(), entity));
    }

    // need to revisit: update should be in a separate hierarchy
    protected N performAdd(N incoming) {
        ClusterPolicy entity = getClusterPolicy();
        performAction(VdcActionType.EditClusterPolicy, getAddParametersProvider().getParameters(incoming, entity));
        entity = getClusterPolicy();
        updateIncomingId(incoming);
        N model = map(entity, incoming);
        return addLinks(doPopulate(model, entity));
    }

    @Override
    protected N doPopulate(N model, ClusterPolicy entity) {
        return model;
    }

    protected ClusterPolicy getClusterPolicy() {
        return getEntity(ClusterPolicy.class,
                VdcQueryType.GetClusterPolicyById,
                new IdQueryParameters(schedulingPolicyId),
                schedulingPolicyId.toString());
    }

}
