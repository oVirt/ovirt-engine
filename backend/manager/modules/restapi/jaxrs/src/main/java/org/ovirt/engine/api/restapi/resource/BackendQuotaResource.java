package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendQuotasResource.SUB_COLLECTIONS;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.QuotaClusterLimitsResource;
import org.ovirt.engine.api.resource.QuotaResource;
import org.ovirt.engine.api.resource.QuotaStorageLimitsResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendQuotaResource extends AbstractBackendSubResource<Quota, org.ovirt.engine.core.common.businessentities.Quota> implements QuotaResource {

    protected BackendQuotasResource parent;

    public BackendQuotasResource getParent() {
        return parent;
    }

    public BackendQuotaResource(String id, BackendQuotasResource parent) {
        super(id,
                Quota.class,
                org.ovirt.engine.core.common.businessentities.Quota.class,
                SUB_COLLECTIONS);
        this.parent = parent;
    }

    @Override
    public Quota get() {
        return addLinks(performGet(VdcQueryType.GetQuotaByQuotaId, new IdQueryParameters(guid)));
    }

    protected class UpdateParametersProvider implements ParametersProvider<Quota, org.ovirt.engine.core.common.businessentities.Quota> {
        @Override
        public VdcActionParametersBase getParameters(Quota incoming,
                org.ovirt.engine.core.common.businessentities.Quota entity) {
            return new QuotaCRUDParameters(map(incoming, entity));
        }
    }

    @Override
    public Quota update(Quota incoming) {
        return performUpdate(incoming,
                new QueryIdResolver<>(VdcQueryType.GetQuotaByQuotaId, IdQueryParameters.class),
                VdcActionType.UpdateQuota,
                new UpdateParametersProvider());
    }

    @Override
    public Response remove() {
        get();
        QuotaCRUDParameters prms = new QuotaCRUDParameters();
        prms.setQuotaId(asGuid(id));
        return performAction(VdcActionType.RemoveQuota, prms);
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
                VdcQueryType.GetPermissionsForObject,
                new GetPermissionsForObjectParameters(guid),
                Quota.class,
                VdcObjectType.Quota));
    }
}
