package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.api.resource.QuotaResource;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendQuotaResource extends AbstractBackendSubResource<Quota, org.ovirt.engine.core.common.businessentities.Quota> implements QuotaResource {

    protected BackendQuotasResource parent;

   public BackendQuotasResource getParent() {
        return parent;
    }

    public BackendQuotaResource(String id, BackendQuotasResource parent) {
        super(id, Quota.class, org.ovirt.engine.core.common.businessentities.Quota.class);
        this.parent = parent;
    }

    @Override
    public Quota get() {
        IdQueryParameters params = new IdQueryParameters(guid);
        Quota quota = performGet(VdcQueryType.GetQuotaByQuotaId, params);
        return quota;
    }

    protected class UpdateParametersProvider implements ParametersProvider<Quota, org.ovirt.engine.core.common.businessentities.Quota> {
        @Override
        public VdcActionParametersBase getParameters(Quota incoming, org.ovirt.engine.core.common.businessentities.Quota entity) {
            return new QuotaCRUDParameters(map(incoming, entity));
        }
    }

    @Override
    protected Quota doPopulate(Quota model, org.ovirt.engine.core.common.businessentities.Quota entity) {
        return model;
    }
}
