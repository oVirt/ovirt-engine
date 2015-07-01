package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.VdsGroupDao;

public class GetAllClustersHavingHostsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private VdsGroupDao clusterDao;

    public GetAllClustersHavingHostsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(clusterDao.getClustersHavingHosts());
    }
}
