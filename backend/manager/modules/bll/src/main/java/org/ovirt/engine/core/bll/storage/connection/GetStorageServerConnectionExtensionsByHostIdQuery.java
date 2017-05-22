package org.ovirt.engine.core.bll.storage.connection;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.StorageServerConnectionExtensionDao;

public class GetStorageServerConnectionExtensionsByHostIdQuery extends QueriesCommandBase<IdQueryParameters> {
    @Inject
    private StorageServerConnectionExtensionDao storageServerConnectionExtensionDao;

    public GetStorageServerConnectionExtensionsByHostIdQuery(IdQueryParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(storageServerConnectionExtensionDao.getByHostId(getParameters().getId()));
    }
}
