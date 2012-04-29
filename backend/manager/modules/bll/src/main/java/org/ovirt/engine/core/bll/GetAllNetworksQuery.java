package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetAllNetworkQueryParamenters;
import org.ovirt.engine.core.compat.Guid;

public class GetAllNetworksQuery<P extends GetAllNetworkQueryParamenters> extends QueriesCommandBase<P> {
    public GetAllNetworksQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (getParameters().getStoragePoolId() == null
                || getParameters().getStoragePoolId().equals(Guid.Empty)) {
            getQueryReturnValue().setReturnValue(getDbFacade().getNetworkDAO().getAll());
        } else {
            getQueryReturnValue().setReturnValue(
                    getDbFacade().getNetworkDAO().getAllForDataCenter(getParameters().getStoragePoolId()));
        }
    }
}
