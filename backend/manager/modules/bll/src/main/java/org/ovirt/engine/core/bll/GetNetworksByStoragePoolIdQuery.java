package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.StorageDomainAndPoolQueryParameters;

public class GetNetworksByStoragePoolIdQuery<P extends StorageDomainAndPoolQueryParameters>
        extends QueriesCommandBase<P> {
    public GetNetworksByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getNetworkDao().getAllForDataCenter(
                        getParameters().getStoragePoolId(),
                        getUserID(),
                        getParameters().isFiltered()));
    }
}
