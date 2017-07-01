package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.ClusterDao;

public class GetAllClustersQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private ClusterDao clusterDao;

    public GetAllClustersQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(clusterDao.getAll(getUserID(), getParameters().isFiltered()));
    }
}
