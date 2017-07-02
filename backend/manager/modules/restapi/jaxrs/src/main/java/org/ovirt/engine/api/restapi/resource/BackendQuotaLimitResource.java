package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class BackendQuotaLimitResource<T extends BaseResource> extends AbstractBackendSubResource<T, Quota> {
    private final Guid parentId;

    protected BackendQuotaLimitResource(String id,
            Guid parentId,
            Class<T> modelType) {
        super(id, modelType, Quota.class);
        this.parentId = parentId;
    }

    public T get() {
        return performGet(QueryType.GetQuotaByQuotaId, new IdQueryParameters(parentId));
    }

    @Override
    protected T map(Quota entity, T template) {
        return super.map(entity, createQuotaLimit());
    }

    protected abstract void updateEntityForRemove(Quota entity, Guid id);

    public Response remove() {
        Quota entity = getEntity(Quota.class,
                QueryType.GetQuotaByQuotaId,
                new IdQueryParameters(parentId),
                parentId.toString());
        updateEntityForRemove(entity, asGuid(id));
        return performAction(ActionType.UpdateQuota,
                new QuotaCRUDParameters(entity));
    }

    protected abstract T createQuotaLimit();
}
