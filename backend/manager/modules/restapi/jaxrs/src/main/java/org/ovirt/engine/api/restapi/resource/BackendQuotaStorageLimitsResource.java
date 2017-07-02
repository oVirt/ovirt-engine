package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.QuotaStorageLimit;
import org.ovirt.engine.api.model.QuotaStorageLimits;
import org.ovirt.engine.api.resource.QuotaStorageLimitResource;
import org.ovirt.engine.api.resource.QuotaStorageLimitsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendQuotaStorageLimitsResource
        extends AbstractBackendCollectionResource<QuotaStorageLimit, Quota>
        implements QuotaStorageLimitsResource {

    private Guid quotaId;

    protected BackendQuotaStorageLimitsResource(Guid quotaId) {
        super(QuotaStorageLimit.class, Quota.class);
        this.quotaId = quotaId;
    }

    @Override
    public QuotaStorageLimits list() {
        Quota quota = getQuota();
        QuotaStorageLimits limits = new QuotaStorageLimits();
        if (quota.getGlobalQuotaStorage() != null) {
            addLimit(quotaId.toString(), limits, quota);
        } else if (quota.getQuotaStorages() != null) {
            for (QuotaStorage quotaStorage : quota.getQuotaStorages()) {
                addLimit(quotaStorage.getStorageId().toString(), limits, quota);
            }
        }
        return limits;
    }

    private void addLimit(String id, QuotaStorageLimits limits, Quota quota) {
        QuotaStorageLimit limit = new QuotaStorageLimit();
        limit.setId(id);
        limits.getQuotaStorageLimits().add(addLinks(map(quota, limit)));
    }

    public Response add(QuotaStorageLimit incoming) {
        Quota entity = getQuota();
        QuotaCRUDParameters parameters = new QuotaCRUDParameters(map(incoming, entity));
        performAction(ActionType.UpdateQuota, parameters);
        entity = getQuota();
        updateIncomingId(incoming, entity);
        QuotaStorageLimit model = map(entity, incoming);
        model = addLinks(doPopulate(model, entity));
        return Response.ok(model).build();
    }

    private void updateIncomingId(QuotaStorageLimit incoming, Quota entity) {
        if (incoming.isSetStorageDomain() && incoming.getStorageDomain().isSetId()) {
            incoming.setId(incoming.getStorageDomain().getId());
        } else {
            incoming.setId(entity.getId().toString());
        }
    }

    private Quota getQuota() {
        return getEntity(
            Quota.class,
            QueryType.GetQuotaByQuotaId,
            new IdQueryParameters(quotaId),
            quotaId.toString()
        );
    }

    @Override
    public QuotaStorageLimitResource getLimitResource(String id) {
        return inject(new BackendQuotaStorageLimitResource(id, quotaId));
    }
}
