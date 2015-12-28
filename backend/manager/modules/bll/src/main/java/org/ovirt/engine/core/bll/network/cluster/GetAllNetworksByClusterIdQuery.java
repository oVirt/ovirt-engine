package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetAllNetworksByClusterIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetAllNetworksByClusterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid clusterId = getParameters().getId();
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getNetworkDao()
                .getAllForCluster(clusterId, getUserID(), getParameters().isFiltered()));
    }
}
