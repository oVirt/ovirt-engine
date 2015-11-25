package org.ovirt.engine.core.bll.storage.pool;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetStorageTypesInPoolByPoolIdQuery <P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetStorageTypesInPoolByPoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getStoragePoolDao().getStorageTypesInPool(getParameters().getId()));
    }
}
