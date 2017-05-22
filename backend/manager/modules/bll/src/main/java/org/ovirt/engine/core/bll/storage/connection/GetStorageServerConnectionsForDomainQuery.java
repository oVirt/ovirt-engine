package org.ovirt.engine.core.bll.storage.connection;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;

public class GetStorageServerConnectionsForDomainQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;

    public GetStorageServerConnectionsForDomainQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(storageServerConnectionDao.getAllForDomain(getParameters().getId()));
    }
}
