package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.StoragePoolQueryParametersBase;

public class GetVdsGroupsByStoragePoolIdQuery<P extends StoragePoolQueryParametersBase>
        extends QueriesCommandBase<P> {
    public GetVdsGroupsByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getVdsGroupDAO().getAllForStoragePool(getParameters().getStoragePoolId()));
    }
}
