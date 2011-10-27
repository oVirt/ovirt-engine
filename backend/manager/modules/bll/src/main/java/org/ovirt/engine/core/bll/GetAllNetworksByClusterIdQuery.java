package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.dal.dbbroker.*;
import org.ovirt.engine.core.common.queries.*;

public class GetAllNetworksByClusterIdQuery<P extends VdsGroupQueryParamenters> extends QueriesCommandBase<P> {
    public GetAllNetworksByClusterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid vdsgroupid = getParameters().getVdsGroupId();
        getQueryReturnValue().setReturnValue(DbFacade.getInstance().getNetworkDAO().getAllForCluster(vdsgroupid));
    }
}
