package org.ovirt.engine.core.bll;

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
                .getNetworkDAO()
                .getAllForCluster(vdsgroupid, getUserID(), getParameters().isFiltered()));
    }
}
