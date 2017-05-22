package org.ovirt.engine.core.bll.storage.pool;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetStoragePoolByIdQuery<P extends IdQueryParameters> extends StoragePoolQueryBase<P> {

    public GetStoragePoolByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected Object queryDataCenter() {
        return storagePoolDao.get(getParameters().getId(), getUserID(), getParameters().isFiltered());
    }
}
