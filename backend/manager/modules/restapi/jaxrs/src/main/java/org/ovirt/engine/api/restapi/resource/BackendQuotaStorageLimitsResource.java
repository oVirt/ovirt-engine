package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.QuotaStorageLimit;
import org.ovirt.engine.api.model.QuotaStorageLimits;
import org.ovirt.engine.api.resource.QuotaStorageLimitResource;
import org.ovirt.engine.api.resource.QuotaStorageLimitsResource;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.compat.Guid;

public class BackendQuotaStorageLimitsResource extends BackendQuotaLimitsResource<QuotaStorageLimits, QuotaStorageLimit> implements QuotaStorageLimitsResource {

    protected BackendQuotaStorageLimitsResource(Guid quotaId) {
        super(quotaId, QuotaStorageLimit.class);
    }

    @Override
    public QuotaStorageLimits list() {
        Quota quota = getQuota();
        QuotaStorageLimits limits = new QuotaStorageLimits();
        if (quota.getGlobalQuotaStorage() != null) {
            addLimit(quota.getGlobalQuotaStorage(), quotaId.toString(), limits, quota);
        } else if (quota.getQuotaStorages() != null) {
            for (QuotaStorage quotaStorage : quota.getQuotaStorages()) {
                addLimit(quotaStorage, quotaStorage.getStorageId().toString(), limits, quota);
            }
        }
        return limits;
    }

    private void addLimit(QuotaStorage quotaStorage, String id, QuotaStorageLimits limits, Quota quota) {
        QuotaStorageLimit limit = new QuotaStorageLimit();
        limit.setId(id);
        limits.getQuotaStorageLimits().add(addLinks(map(quota, limit)));
    }

    @Override
    public QuotaStorageLimitResource getLimitResource(String id) {
        return inject(new BackendQuotaStorageLimitResource(id, quotaId));
    }

    @Override
    protected void updateIncomingId(QuotaStorageLimit incoming, Quota entity) {
        if (incoming.isSetStorageDomain() && incoming.getStorageDomain().isSetId()) {
            incoming.setId(incoming.getStorageDomain().getId());
        } else {
            incoming.setId(entity.getId().toString());
        }
    }
}
