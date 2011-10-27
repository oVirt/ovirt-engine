package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetStorageServerConnectionsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetStorageServerConnectionsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance().getStorageServerConnectionDAO().getAll());
    }
}
