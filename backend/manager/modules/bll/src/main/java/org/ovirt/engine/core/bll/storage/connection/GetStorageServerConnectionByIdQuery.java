package org.ovirt.engine.core.bll.storage.connection;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetStorageServerConnectionByIdQuery<P extends StorageServerConnectionQueryParametersBase>
        extends QueriesCommandBase<P> {
    public GetStorageServerConnectionByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance().
                getStorageServerConnectionDao().get(getParameters().
                                                                            getServerConnectionId()));
    }
}
