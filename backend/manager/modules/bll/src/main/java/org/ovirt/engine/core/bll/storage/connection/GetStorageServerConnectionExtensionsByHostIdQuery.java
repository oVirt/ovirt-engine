package org.ovirt.engine.core.bll.storage.connection;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.StorageServerConnectionExtensionDao;

public class GetStorageServerConnectionExtensionsByHostIdQuery extends QueriesCommandBase<IdQueryParameters> {
    @Inject
    private StorageServerConnectionExtensionDao storageServerConnectionExtensionDao;

    public GetStorageServerConnectionExtensionsByHostIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(storageServerConnectionExtensionDao.getByHostId(getParameters().getId()));
    }
}
