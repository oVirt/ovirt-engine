package org.ovirt.engine.core.bll.storage.pool;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.dao.StoragePoolDao;

public class GetAllStoragePoolsQuery<P extends QueryParametersBase> extends StoragePoolQueryBase<P> {
    @Inject
    private StoragePoolDao storagePoolDao;

    public GetAllStoragePoolsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected Object queryDataCenter() {
        return storagePoolDao.getAll(getUserID(), getParameters().isFiltered());
    }
}
