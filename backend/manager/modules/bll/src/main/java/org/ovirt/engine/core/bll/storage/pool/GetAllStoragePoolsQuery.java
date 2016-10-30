package org.ovirt.engine.core.bll.storage.pool;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.StoragePoolDao;

public class GetAllStoragePoolsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private StoragePoolDao storagePoolDao;

    public GetAllStoragePoolsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(storagePoolDao.getAll(getUserID(), getParameters().isFiltered()));
    }
}
