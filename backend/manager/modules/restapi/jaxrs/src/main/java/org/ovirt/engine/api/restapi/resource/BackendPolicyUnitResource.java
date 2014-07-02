package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.resource.PolicyUnitResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;

public abstract class BackendPolicyUnitResource<T extends BaseResource> extends AbstractBackendSubResource<T, ClusterPolicy> implements
        PolicyUnitResource<T> {
    private static final String[] SUB_COLLECTIONS = {};
    private final Guid parentId;

    protected BackendPolicyUnitResource(String id,
            Guid parentId,
            Class<T> modelType) {
        super(id, modelType, ClusterPolicy.class, SUB_COLLECTIONS);
        this.parentId = parentId;
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
    protected T doPopulate(T model, ClusterPolicy entity) {
        return model;
    }

    protected abstract T createPolicyUnitByType();
}
