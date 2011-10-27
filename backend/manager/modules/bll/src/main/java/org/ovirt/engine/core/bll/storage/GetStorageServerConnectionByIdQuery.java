package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetStorageServerConnectionByIdQuery<P extends StorageServerConnectionQueryParametersBase>
        extends QueriesCommandBase<P> {
    public GetStorageServerConnectionByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance().
                getStorageServerConnectionDAO().get(getParameters().
                                                                            getServerConnectionId()));
    }
}
