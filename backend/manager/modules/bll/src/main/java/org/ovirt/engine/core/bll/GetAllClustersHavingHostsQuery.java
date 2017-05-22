package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.ClusterDao;

public class GetAllClustersHavingHostsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private ClusterDao clusterDao;

    public GetAllClustersHavingHostsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(clusterDao.getClustersHavingHosts());
    }
}
