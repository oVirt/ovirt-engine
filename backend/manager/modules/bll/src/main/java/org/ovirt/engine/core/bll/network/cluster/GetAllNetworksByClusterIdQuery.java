package org.ovirt.engine.core.bll.network.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class GetAllNetworksByClusterIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private NetworkDao networkDao;

    public GetAllNetworksByClusterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid clusterId = getParameters().getId();
        getQueryReturnValue().setReturnValue(
                networkDao.getAllForCluster(clusterId, getUserID(), getParameters().isFiltered()));
    }
}
