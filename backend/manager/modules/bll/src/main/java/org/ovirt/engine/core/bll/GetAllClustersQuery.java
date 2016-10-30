package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.ClusterDao;

public class GetAllClustersQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private ClusterDao clusterDao;

    public GetAllClustersQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(clusterDao.getAll(getUserID(), getParameters().isFiltered()));
    }
}
