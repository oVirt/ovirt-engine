package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.StoragePoolQueryParametersBase;

public class GetStoragePoolByIdQuery<P extends StoragePoolQueryParametersBase> extends QueriesCommandBase<P> {
    public GetStoragePoolByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue()
                .setReturnValue(getDbFacade()
                        .getStoragePoolDAO()
                        .get(getParameters().getStoragePoolId(), getUserID(), getParameters().isFiltered()));
    }
}
