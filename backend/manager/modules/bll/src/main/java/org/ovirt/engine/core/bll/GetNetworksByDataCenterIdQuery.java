package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetNetworksByDataCenterIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetNetworksByDataCenterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getNetworkDao().getAllForDataCenter(getParameters().getId(),
                getUserID(),
                getParameters().isFiltered()));
    }
}
