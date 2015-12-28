package org.ovirt.engine.api.restapi.resource;

import java.util.Iterator;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.QuotaClusterLimit;
import org.ovirt.engine.api.resource.QuotaClusterLimitResource;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.compat.Guid;

public class BackendQuotaClusterLimitResource extends BackendQuotaLimitResource<QuotaClusterLimit> implements
        QuotaClusterLimitResource {

    protected BackendQuotaClusterLimitResource(String id, Guid parentId) {
        super(id, parentId, QuotaClusterLimit.class);
    }

    @Override
    protected QuotaClusterLimit createQuotaLimit() {
        QuotaClusterLimit limit = new QuotaClusterLimit();
        limit.setId(id);
        return limit;
    }

    @Override
    protected void updateEntityForRemove(Quota entity, Guid id) {
        // since we're mocking remove using update, we'll throw 404 if the object isn't found
        boolean throw404 = false;
        // global cluster (has same id as quota)
        if (entity.getId().equals(id)) {
            if (entity.getGlobalQuotaCluster() == null) {
                throw404 = true;
            } else {
                entity.setGlobalQuotaCluster(null);
            }
            // specific cluster (has same id as cluster)
        } else {
            if (entity.getQuotaClusters() != null) {
                Iterator<QuotaCluster> iterator = entity.getQuotaClusters().iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().getClusterId().equals(id)) {
                        iterator.remove();
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
