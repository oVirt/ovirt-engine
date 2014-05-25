package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.resource.PolicyUnitResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;

public abstract class BackendPolicyUnitResource<T extends BaseResource> extends AbstractBackendSubResource<T, ClusterPolicy> implements
        PolicyUnitResource<T> {
    private static final String[] SUB_COLLECTIONS = {};

    protected BackendPolicyUnitResource(String id,
            Class<T> modelType) {
        super(id, modelType, ClusterPolicy.class, SUB_COLLECTIONS);
    }

    @Override
    public T get() {
        return performGet(VdcQueryType.GetPolicyUnitById, new IdQueryParameters(guid));
    }

    @Override
    protected T doPopulate(T model, ClusterPolicy entity) {
        return model;
    }

}
