package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.dal.dbbroker.*;
import org.ovirt.engine.core.common.queries.*;

public class GetAllNetworksQuery<P extends GetAllNetworkQueryParamenters> extends QueriesCommandBase<P> {
    public GetAllNetworksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (getParameters().getStoragePoolId() == null
                || getParameters().getStoragePoolId().equals(Guid.Empty)) {
            getQueryReturnValue().setReturnValue(DbFacade.getInstance().getNetworkDAO().getAll());
        } else {
            getQueryReturnValue().setReturnValue(
                    DbFacade.getInstance().getNetworkDAO().getAllForDataCenter(
                            getParameters().getStoragePoolId()));
        }
    }
}
