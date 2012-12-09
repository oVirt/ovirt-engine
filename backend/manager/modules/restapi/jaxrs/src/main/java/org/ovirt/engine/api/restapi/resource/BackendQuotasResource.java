package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.api.model.Quotas;
import org.ovirt.engine.api.resource.QuotaResource;
import org.ovirt.engine.api.resource.QuotasResource;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetQuotaByQuotaIdQueryParameters;
import org.ovirt.engine.core.common.queries.GetQuotaByStoragePoolIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendQuotasResource extends AbstractBackendCollectionResource<Quota, org.ovirt.engine.core.common.businessentities.Quota> implements QuotasResource {

    protected Guid dataCenterId;

    protected BackendQuotasResource(String datacenterId) {
        super(Quota.class, org.ovirt.engine.core.common.businessentities.Quota.class);
        this.dataCenterId = asGuid(datacenterId);
    }

    @Override
    public Quotas list() {
        GetQuotaByStoragePoolIdQueryParameters params = new GetQuotaByStoragePoolIdQueryParameters();
        params.setStoragePoolId(dataCenterId);
        return mapCollection(getBackendCollection(VdcQueryType.GetQuotaByStoragePoolId, params));
    }

    @Override
    public Response add(Quota quota) {
        validateParameters(quota, "name");
        org.ovirt.engine.core.common.businessentities.Quota entity = map(quota);
        entity.setStoragePoolId(dataCenterId);
        return performCreate(VdcActionType.AddQuota,
                new QuotaCRUDParameters(entity),
                new QueryIdResolver<Guid>(VdcQueryType.GetQuotaByQuotaId, GetQuotaByQuotaIdQueryParameters.class));
    }

    @Override
    protected Response performRemove(String id) {
        QuotaCRUDParameters removeParams = new QuotaCRUDParameters();
        removeParams.setQuotaId(asGuid(id));
        removeParams.setStoragePoolId(dataCenterId);
        return performAction(VdcActionType.RemoveQuota, removeParams);
    }

    @Override
    @SingleEntityResource
    public QuotaResource getQuotaSubResource(String id) {
        return inject(new BackendQuotaResource(id, this));
    }

    protected Quotas mapCollection(List<org.ovirt.engine.core.common.businessentities.Quota> entities) {
        Quotas collection = new Quotas();
        for (org.ovirt.engine.core.common.businessentities.Quota entity : entities) {
            collection.getQuotas().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    @Override
    protected Quota addParents(Quota quota) {
        quota.setDataCenter(new DataCenter());
        quota.getDataCenter().setId(dataCenterId.toString());
        return quota;
    }

    @Override
    protected Quota doPopulate(Quota model, org.ovirt.engine.core.common.businessentities.Quota entity) {
        return model;
    }
}
