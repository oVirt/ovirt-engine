package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.resource.PolicyUnitResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public abstract class BackendPolicyUnitResource<T extends BaseResource> extends AbstractBackendSubResource<T, ClusterPolicy> implements
        PolicyUnitResource<T> {
    private static final String[] SUB_COLLECTIONS = {};
    private final Guid parentId;
    private final BackendPolicyUnitsResource<?, ?> parent;

    protected BackendPolicyUnitResource(String id,
            BackendPolicyUnitsResource<?, ?> parent,
            Class<T> modelType) {
        super(id, modelType, ClusterPolicy.class, SUB_COLLECTIONS);
        this.parent = parent;
        this.parentId = parent.schedulingPolicyId;
    }

    @Override
    public T get() {
        return performGet(VdcQueryType.GetClusterPolicyById, new IdQueryParameters(parentId));
    }

    @Override
    protected T map(ClusterPolicy entity, T template) {
        return super.map(entity, createPolicyUnitByType());
    }

    @Override
    public Response remove() {
        ClusterPolicy entity = parent.getClusterPolicy();
        updateEntityForRemove(entity, asGuid(id));
        return performAction(VdcActionType.EditClusterPolicy,
                new ClusterPolicyCRUDParameters(entity.getId(), entity));
    }

    protected abstract void updateEntityForRemove(ClusterPolicy entity, Guid id);

    protected abstract T createPolicyUnitByType();
}
