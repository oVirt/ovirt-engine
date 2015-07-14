package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.QuotaStorageLimit;
import org.ovirt.engine.api.resource.QuotaStorageLimitResource;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.compat.Guid;

public class BackendQuotaStorageLimitResource extends BackendQuotaLimitResource<QuotaStorageLimit> implements
        QuotaStorageLimitResource {

    protected BackendQuotaStorageLimitResource(String id, Guid parentId) {
        super(id, parentId, QuotaStorageLimit.class);
    }

    @Override
    protected QuotaStorageLimit createQuotaLimit() {
        QuotaStorageLimit limit = new QuotaStorageLimit();
        limit.setId(id);
        return limit;
    }

    @Override
    protected void updateEntityForRemove(Quota entity, Guid id) {
        // since we're mocking remove using update, we'll throw 404 if the object isn't found
        boolean throw404 = false;
        // global storage (has same id as quota)
        if (entity.getId().equals(id)) {
            if (entity.getGlobalQuotaStorage() == null) {
                throw404 = true;
            } else {
                entity.setGlobalQuotaStorage(null);
            }
            // specific storage (has same id as storage domain)
        } else {
            if (entity.getQuotaStorages() != null) {
                for (int i = 0; i < entity.getQuotaStorages().size(); i++) {
                    if (entity.getQuotaStorages().get(i).getStorageId().equals(id)) {
                        entity.getQuotaStorages().remove(i);
                        return;
                    }
                }
                throw404 = true;
            }
        }
        if (throw404) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }
}
