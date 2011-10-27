package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetStoragePoolByIdQuery<P extends StoragePoolQueryParametersBase> extends QueriesCommandBase<P> {
    public GetStoragePoolByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue()
                .setReturnValue(DbFacade.getInstance().getStoragePoolDAO().get(getParameters().getStoragePoolId()));
    }
}
