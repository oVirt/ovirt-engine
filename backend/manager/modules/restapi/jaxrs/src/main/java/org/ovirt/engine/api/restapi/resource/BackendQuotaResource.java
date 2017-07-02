package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.QuotaClusterLimitsResource;
import org.ovirt.engine.api.resource.QuotaResource;
import org.ovirt.engine.api.resource.QuotaStorageLimitsResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendQuotaResource extends AbstractBackendSubResource<Quota, org.ovirt.engine.core.common.businessentities.Quota> implements QuotaResource {

    protected BackendQuotasResource parent;

    public BackendQuotasResource getParent() {
        return parent;
    }

    public BackendQuotaResource(String id, BackendQuotasResource parent) {
        super(id,
                Quota.class,
                org.ovirt.engine.core.common.businessentities.Quota.class);
        this.parent = parent;
    }

    @Override
    public Quota get() {
        return addLinks(performGet(QueryType.GetQuotaByQuotaId, new IdQueryParameters(guid)));
    }

    protected class UpdateParametersProvider implements ParametersProvider<Quota, org.ovirt.engine.core.common.businessentities.Quota> {
        @Override
        public ActionParametersBase getParameters(Quota incoming,
                org.ovirt.engine.core.common.businessentities.Quota entity) {
            return new QuotaCRUDParameters(map(incoming, entity));
        }
    }

    @Override
    public Quota update(Quota incoming) {
        return performUpdate(incoming,
                new QueryIdResolver<>(QueryType.GetQuotaByQuotaId, IdQueryParameters.class),
                ActionType.UpdateQuota,
                new UpdateParametersProvider());
    }

    @Override
    public Response remove() {
        get();
        IdParameters prms = new IdParameters(asGuid(id));
        return performAction(ActionType.RemoveQuota, prms);
    }

    @Override
    public QuotaStorageLimitsResource getQuotaStorageLimitsResource() {
        return inject(new BackendQuotaStorageLimitsResource(guid));
    }

    @Override
    public QuotaClusterLimitsResource getQuotaClusterLimitsResource() {
        return inject(new BackendQuotaClusterLimitsResource(guid));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                QueryType.GetPermissionsForObject,
                new GetPermissionsForObjectParameters(guid),
                Quota.class,
                VdcObjectType.Quota));
    }
}
