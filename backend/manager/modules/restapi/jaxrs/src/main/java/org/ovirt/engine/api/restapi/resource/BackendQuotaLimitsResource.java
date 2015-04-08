package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.BaseResources;
import org.ovirt.engine.api.resource.QuotaLimitsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class BackendQuotaLimitsResource<M extends BaseResources, N extends BaseResource> extends AbstractBackendCollectionResource<N, Quota> implements QuotaLimitsResource<M, N> {

    protected final Guid quotaId;

    protected BackendQuotaLimitsResource(Guid quotaId,
            Class<N> baseResourcesClass) {
        super(baseResourcesClass, Quota.class);
        this.quotaId = quotaId;
    }

    protected abstract void updateIncomingId(N incoming, Quota entity);

    protected ParametersProvider<N, Quota> getAddParametersProvider() {
        return new ParametersProvider<N, Quota>() {
            @Override
            public VdcActionParametersBase getParameters(N model, Quota entity) {
                return new QuotaCRUDParameters(map(model, entity));
            }
        };
    }

    @Override
    public N add(N incoming) {
        Quota entity = getQuota();
        performAction(VdcActionType.UpdateQuota, getAddParametersProvider().getParameters(incoming, entity));
        entity = getQuota();
        updateIncomingId(incoming, entity);
        N model = map(entity, incoming);
        return addLinks(doPopulate(model, entity));
    }

    @Override
    protected N doPopulate(N model, Quota entity) {
        return model;
    }

    protected Quota getQuota() {
        return getEntity(Quota.class,
                VdcQueryType.GetQuotaByQuotaId,
                new IdQueryParameters(quotaId),
                quotaId.toString());
    }

}
