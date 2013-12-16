package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetNetworksByStoragePoolIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetNetworksByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getNetworkDao().getAllForDataCenter(getParameters().getId(),
                getUserID(),
                getParameters().isFiltered()));
    }
}
