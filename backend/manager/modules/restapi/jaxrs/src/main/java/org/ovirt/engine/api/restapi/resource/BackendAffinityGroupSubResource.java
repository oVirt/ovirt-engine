package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.core.compat.Guid;

public abstract class BackendAffinityGroupSubResource<R extends BaseResource, Q>
        extends AbstractBackendActionableResource<R, Q> {

    private final Guid affinityGroupId;

    public BackendAffinityGroupSubResource(Guid affinityGroupId, String id, Class<R> modelType, Class<Q> entityType) {
        super(id, modelType, entityType);
        this.affinityGroupId = affinityGroupId;
    }

    public Guid getAffinityGroupId() {
        return affinityGroupId;
    }

}
