package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.compat.Guid;

public class GetAllNetworksByClusterIdQuery<P extends VdsGroupQueryParamenters> extends QueriesCommandBase<P> {
    public GetAllNetworksByClusterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid vdsgroupid = getParameters().getVdsGroupId();
        getQueryReturnValue().setReturnValue(getDbFacade()
                .getNetworkDao()
                .getAllForCluster(vdsgroupid, getUserID(), getParameters().isFiltered()));
    }
}
