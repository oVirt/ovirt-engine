package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.QuotaClusterLimit;
import org.ovirt.engine.api.model.QuotaClusterLimits;
import org.ovirt.engine.api.resource.QuotaClusterLimitResource;
import org.ovirt.engine.api.resource.QuotaClusterLimitsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendQuotaClusterLimitsResource
        extends AbstractBackendCollectionResource<QuotaClusterLimit, Quota>
        implements QuotaClusterLimitsResource {

    private Guid quotaId;

    protected BackendQuotaClusterLimitsResource(Guid quotaId) {
        super(QuotaClusterLimit.class, Quota.class);
        this.quotaId = quotaId;
    }

    @Override
    public QuotaClusterLimits list() {
        Quota quota = getQuota();
        QuotaClusterLimits limits = new QuotaClusterLimits();
        if (quota.getGlobalQuotaCluster() != null) {
            addLimit(quotaId.toString(), limits, quota);
        } else if (quota.getQuotaClusters() != null) {
            for (QuotaCluster quotaCluster : quota.getQuotaClusters()) {
                addLimit(quotaCluster.getClusterId().toString(), limits, quota);
            }
        }
        return limits;
    }

    private void addLimit(String id, QuotaClusterLimits limits, Quota quota) {
        QuotaClusterLimit limit = new QuotaClusterLimit();
        limit.setId(id);
        limits.getQuotaClusterLimits().add(addLinks(map(quota, limit)));
    }

    @Override
    public Response add(QuotaClusterLimit incoming) {
        Quota entity = getQuota();
        QuotaCRUDParameters parameters = new QuotaCRUDParameters(map(incoming, entity));
        performAction(ActionType.UpdateQuota, parameters);
        entity = getQuota();
        updateIncomingId(incoming, entity);
        QuotaClusterLimit model = map(entity, incoming);
        model = addLinks(doPopulate(model, entity));
        return Response.ok(model).build();
    }

    @Override
    public QuotaClusterLimitResource getLimitResource(String id) {
        return inject(new BackendQuotaClusterLimitResource(id, quotaId));
    }

    private void updateIncomingId(QuotaClusterLimit incoming, Quota entity) {
        if (incoming.isSetCluster() && incoming.getCluster().isSetId()) {
            incoming.setId(incoming.getCluster().getId());
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
}
