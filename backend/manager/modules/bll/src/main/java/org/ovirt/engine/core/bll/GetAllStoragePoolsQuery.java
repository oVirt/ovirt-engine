package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllStoragePoolsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetAllStoragePoolsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getStoragePoolDao()
                .getAll(getUserID(), getParameters().isFiltered()));
    }
}
