package org.ovirt.engine.core.bll.storage.pool;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.NameQueryParameters;

public class GetStoragePoolByDatacenterNameQuery<P extends NameQueryParameters> extends StoragePoolQueryBase<P> {
    public GetStoragePoolByDatacenterNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected List<StoragePool> queryDataCenter() {
        return storagePoolDao.getByName(getParameters().getName(), true);
    }
}
