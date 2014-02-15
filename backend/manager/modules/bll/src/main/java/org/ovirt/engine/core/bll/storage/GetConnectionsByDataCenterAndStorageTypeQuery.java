package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.GetConnectionsByDataCenterAndStorageTypeParameters;

public class GetConnectionsByDataCenterAndStorageTypeQuery<P extends GetConnectionsByDataCenterAndStorageTypeParameters>
        extends QueriesCommandBase<P> {

    public GetConnectionsByDataCenterAndStorageTypeQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getStorageServerConnectionDao()
                        .getConnectableStorageConnectionsByStorageType(getParameters().getId(), getParameters().getStorageType()));
    }
}
