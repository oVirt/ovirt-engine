package org.ovirt.engine.core.bll.storage.connection;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;

public class GetAllStorageServerConnectionsQuery <P extends VdcQueryParametersBase> extends QueriesCommandBase<P>  {
    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;

    public GetAllStorageServerConnectionsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(storageServerConnectionDao.getAll());
    }
}
