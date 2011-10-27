package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetVdsGroupsByStoragePoolIdQuery<P extends StoragePoolQueryParametersBase>
        extends QueriesCommandBase<P> {
    public GetVdsGroupsByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getVdsGroupDAO().getAllForStoragePool(getParameters().getStoragePoolId()));
    }
}
