package org.ovirt.engine.core.bll.storage.pool;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.StoragePoolDao;

public class GetAllStoragePoolsQuery<P extends VdcQueryParametersBase> extends StoragePoolQueryBase<P> {
    @Inject
    private StoragePoolDao storagePoolDao;

    public GetAllStoragePoolsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected Object queryDataCenter() {
        return storagePoolDao.getAll(getUserID(), getParameters().isFiltered());
    }
}
