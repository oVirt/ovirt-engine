package org.ovirt.engine.core.bll;


import org.ovirt.engine.core.common.queries.IdQueryParameters;


public class GetAllNetworkQosByStoragePoolIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetAllNetworkQosByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getNetworkQosDao().getAllForStoragePoolId(getParameters().getId()));
    }
}
