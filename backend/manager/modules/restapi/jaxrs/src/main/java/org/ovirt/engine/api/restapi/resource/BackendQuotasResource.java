package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.api.model.Quotas;
import org.ovirt.engine.api.resource.QuotaResource;
import org.ovirt.engine.api.resource.QuotasResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendQuotasResource extends AbstractBackendCollectionResource<Quota, org.ovirt.engine.core.common.businessentities.Quota> implements QuotasResource {

    protected Guid dataCenterId;

    protected BackendQuotasResource(String datacenterId) {
        super(Quota.class, org.ovirt.engine.core.common.businessentities.Quota.class);
        this.dataCenterId = asGuid(datacenterId);
    }

    @Override
    public Quotas list() {
        if (isFiltered()) {
            return null;
        } else {
            IdQueryParameters params = new IdQueryParameters(dataCenterId);
            return mapCollection(getBackendCollection(QueryType.GetQuotaByStoragePoolId, params));
        }
    }

    @Override
    public Response add(Quota quota) {
        validateParameters(quota, "name");
        org.ovirt.engine.core.common.businessentities.Quota entity = map(quota);
        entity.setStoragePoolId(dataCenterId);
        return performCreate(ActionType.AddQuota,
                new QuotaCRUDParameters(entity),
                new QueryIdResolver<Guid>(QueryType.GetQuotaByQuotaId, IdQueryParameters.class));
    }

    @Override
    public QuotaResource getQuotaResource(String id) {
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
}
