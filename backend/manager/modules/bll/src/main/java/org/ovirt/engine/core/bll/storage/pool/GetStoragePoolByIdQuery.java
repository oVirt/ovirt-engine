package org.ovirt.engine.core.bll.storage.pool;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetStoragePoolByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetStoragePoolByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue()
                .setReturnValue(getDbFacade()
                        .getStoragePoolDao()
                        .get(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
