package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetAllNetworksByClusterIdQuery<P extends VdsGroupQueryParamenters> extends QueriesCommandBase<P> {
    public GetAllNetworksByClusterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid vdsgroupid = getParameters().getVdsGroupId();
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getNetworkDAO()
                .getAllForCluster(vdsgroupid, getUserID(), getParameters().isFiltered()));
    }
}
