package org.ovirt.engine.core.bll.storage.connection;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetConnectableStorageServerConnectionsByStoragePoolIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetConnectableStorageServerConnectionsByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getStorageServerConnectionDao()
                        .getAllConnectableStorageSeverConnection(getParameters().getId()));
    }
}
