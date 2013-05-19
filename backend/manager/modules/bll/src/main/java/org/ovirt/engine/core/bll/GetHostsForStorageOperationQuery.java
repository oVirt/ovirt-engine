package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetHostsForStorageOperationParameters;

public class GetHostsForStorageOperationQuery<P extends GetHostsForStorageOperationParameters> extends QueriesCommandBase<P> {
    public GetHostsForStorageOperationQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getVdsDao().getHostsForStorageOperation(
                getParameters().getId(), getParameters().isLocalFsOnly()));
    }
}
