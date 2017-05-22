package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.ClusterDao;

public class GetClustersByStoragePoolIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    @Inject
    private ClusterDao clusterDao;

    public GetClustersByStoragePoolIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                clusterDao.getAllForStoragePool(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
